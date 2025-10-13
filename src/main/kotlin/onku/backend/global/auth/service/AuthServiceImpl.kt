package onku.backend.global.auth.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.service.MemberService
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.dto.*
import onku.backend.global.auth.jwt.JwtUtil
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
    private val memberProfileRepository: MemberProfileRepository,
    private val jwtUtil: JwtUtil,
    private val refreshTokenCacheUtil: RefreshTokenCache,
    @Value("\${jwt.refresh-ttl}") private val refreshTtl: Duration,
    @Value("\${jwt.onboarding-ttl}") private val onboardingTtl: Duration,
) : AuthService {

    private fun rolesFor(member: Member): List<String> =
        when (member.role) {
            Role.ADMIN -> listOf("ADMIN", "USER")
            Role.USER  -> listOf("USER")
        }

    @Transactional
    override fun kakaoLogin(dto: KakaoLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>> {
        val token = kakaoService.getAccessToken(dto.code)
        val profile = kakaoService.getProfile(token.accessToken)

        val socialId = profile.id.toString()
        val email = profile.kakaoAccount?.email
            ?: throw CustomException(AuthErrorCode.OAUTH_EMAIL_SCOPE_REQUIRED)

        val member = memberService.upsertSocialMember(
            email = email,
            socialId = socialId,
            type = SocialType.KAKAO
        )

        return when (member.approval) {
            ApprovalStatus.APPROVED -> {
                val roles = rolesFor(member)
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
                                role = member.role.name
                            )
                        )
                    )
            }

            ApprovalStatus.PENDING -> {
                if (member.hasInfo) { // 이미 프로필이 있으면 온보딩 토큰 미발급
                    ResponseEntity
                        .status(HttpStatus.ACCEPTED)
                        .body(SuccessResponse.ok(AuthLoginResult(status = ApprovalStatus.PENDING)))
                } else {
                    val onboarding = jwtUtil.createOnboardingToken(email, onboardingTtl.toMinutes())
                    val headers = HttpHeaders().apply {
                        add(HttpHeaders.AUTHORIZATION, "Bearer $onboarding")
                    }

                    ResponseEntity
                        .status(HttpStatus.OK)
                        .headers(headers)
                        .body(SuccessResponse.ok(AuthLoginResult(status = ApprovalStatus.PENDING)))
                }
            }

            ApprovalStatus.REJECTED -> {
                ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(SuccessResponse.ok(AuthLoginResult(status = ApprovalStatus.REJECTED)))
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

        val roles = rolesFor(member)
        val newAccess = jwtUtil.createAccessToken(email, roles)

        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $newAccess")
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(headers)
            .body(SuccessResponse.ok("Access Token이 재발급되었습니다."))
    }
}
