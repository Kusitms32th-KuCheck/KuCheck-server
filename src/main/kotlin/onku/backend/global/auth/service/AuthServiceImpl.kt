package onku.backend.global.auth.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.service.MemberService
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.dto.AuthLoginResult
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.config.KakaoProps
import onku.backend.global.exception.CustomException
import onku.backend.global.redis.cache.RefreshTokenCache
import onku.backend.global.response.SuccessResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

@Service
@Transactional(readOnly = true)
class AuthServiceImpl(
    private val memberService: MemberService,
    private val kakaoService: KakaoService,
    private val jwtUtil: JwtUtil,
    private val refreshTokenCacheUtil: RefreshTokenCache,
    @Value("\${jwt.refresh-ttl}") private val refreshTtl: Duration,
    @Value("\${jwt.onboarding-ttl}") private val onboardingTtl: Duration,
    private val kakaoProps: KakaoProps,
) : AuthService {

    @Transactional
    override fun kakaoLogin(dto: KakaoLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>> {
        val redirectUri = kakaoProps.redirectMap[dto.env]
            ?: throw CustomException(AuthErrorCode.INVALID_REDIRECT_URI)

        val token = kakaoService.getAccessToken(
            code = dto.code,
            redirectUri = redirectUri,
            clientId = kakaoProps.clientId
        )
        val profile = kakaoService.getProfile(token.accessToken)

        val socialId = profile.id
        val email = profile.kakaoAccount?.email
            ?: throw CustomException(AuthErrorCode.OAUTH_EMAIL_SCOPE_REQUIRED)

        val member = memberService.upsertSocialMember(
            email = email,
            socialId = socialId,
            type = SocialType.KAKAO
        )

        return when (member.approval) {
            ApprovalStatus.APPROVED -> {
                val roles = member.role.authorities()
                val access = jwtUtil.createAccessToken(email, roles)
                val refresh = jwtUtil.createRefreshToken(email, roles)
                refreshTokenCacheUtil.saveRefreshToken(email, refresh, refreshTtl)

                val headers = HttpHeaders().apply {
                    add(HttpHeaders.AUTHORIZATION, "Bearer $access")
                    add("X-Refresh-Token", refresh)
                }

                ResponseEntity
                    .status(HttpStatus.OK)
                    .headers(headers)
                    .body(
                        SuccessResponse.ok(
                            AuthLoginResult(
                                status = ApprovalStatus.APPROVED,
                                memberId = member.id,
                                role = member.role,
                                hasInfo = member.hasInfo
                            )
                        )
                    )
            }

            ApprovalStatus.PENDING -> {
                if (member.hasInfo) {
                    // 온보딩 제출 완료(프로필 있음) → 온보딩 토큰 미발급
                    ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(
                            SuccessResponse.ok(
                                AuthLoginResult(
                                    status = ApprovalStatus.PENDING,
                                    memberId = member.id,
                                    role = member.role,
                                    hasInfo = true
                                )
                            )
                        )
                } else {
                    // 온보딩 제출 전(프로필 없음) → 온보딩 토큰 발급
                    val onboarding = jwtUtil.createOnboardingToken(email, onboardingTtl.toMinutes())
                    val headers = HttpHeaders().apply {
                        add(HttpHeaders.AUTHORIZATION, "Bearer $onboarding")
                    }
                    ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(headers)
                        .body(
                            SuccessResponse.ok(
                                AuthLoginResult(
                                    status = ApprovalStatus.PENDING,
                                    memberId = member.id,
                                    role = member.role,
                                    hasInfo = false
                                )
                            )
                        )
                }
            }

            ApprovalStatus.REJECTED -> {
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(
                        SuccessResponse.ok(
                            AuthLoginResult(
                                status = ApprovalStatus.REJECTED,
                                memberId = member.id,
                                role = member.role,
                                hasInfo = member.hasInfo
                            )
                        )
                    )
            }
        }
    }

    @Transactional(readOnly = true)
    override fun reissueAccessToken(refreshToken: String): ResponseEntity<SuccessResponse<String>> {
        if (jwtUtil.isExpired(refreshToken)) {
            throw CustomException(AuthErrorCode.EXPIRED_REFRESH_TOKEN)
        }
        val email = jwtUtil.getEmail(refreshToken)
        val stored = refreshTokenCacheUtil.getRefreshToken(email)
            ?: throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        if (stored != refreshToken) throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val member = memberService.getByEmail(email)
        if (member.approval != ApprovalStatus.APPROVED) {
            throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }

        val roles = member.role.authorities()
        val newAccess = jwtUtil.createAccessToken(email, roles)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $newAccess")
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(headers)
            .body(SuccessResponse.ok("Access Token이 재발급되었습니다."))
    }

    @Transactional
    override fun logout(refreshToken: String): ResponseEntity<SuccessResponse<String>> {
        deleteRefreshTokenBy(refreshToken)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(SuccessResponse.ok("로그아웃 되었습니다."))
    }

    @Transactional
    override fun withdraw(member: Member, refreshToken: String): ResponseEntity<SuccessResponse<String>> {
        kakaoService.adminUnlink(member.socialId, kakaoProps.adminKey)
        deleteRefreshTokenBy(refreshToken)
        val memberId = member.id ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
        memberService.deleteMemberById(memberId)
        return ResponseEntity.ok(SuccessResponse.ok("회원 탈퇴가 완료되었습니다."))
    }

    private fun deleteRefreshTokenBy(refreshToken: String): String? {
        val email = runCatching { jwtUtil.getEmail(refreshToken) }.getOrNull() ?: return null
        refreshTokenCacheUtil.deleteRefreshToken(email)
        return email
    }
}