package onku.backend.global.auth.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.service.MemberService
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.exception.CustomException
import onku.backend.global.redis.RefreshTokenCacheUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

interface AuthService {
    fun reissueAccessToken(refreshToken: String): String
    fun kakaoLogin(dto: KakaoLoginRequest): ResponseEntity<Any>
}

@Service
@Transactional(readOnly = true)
class AuthServiceImpl(
    private val memberService: MemberService,
    private val kakaoService: KakaoService,
    private val jwtUtil: JwtUtil,
    private val refreshTokenCacheUtil: RefreshTokenCacheUtil
) : AuthService {

    private fun rolesFor(member: Member): List<String> =
        when (member.role) {
            Role.ADMIN -> listOf("ADMIN", "USER")
            Role.USER  -> listOf("USER")
        }

    @Transactional
    override fun kakaoLogin(dto: KakaoLoginRequest): ResponseEntity<Any> {
        val token = kakaoService.getAccessToken(dto.code)
        val profile = kakaoService.getProfile(token.accessToken)

        val socialId: String = profile.id.toString()
        val email: String = profile.kakaoAccount?.email
            ?: throw CustomException(AuthErrorCode.OAUTH_EMAIL_SCOPE_REQUIRED)

        val member: Member = memberService.upsertSocialMember(
            email = email,
            socialId = socialId,
            type = SocialType.KAKAO
        )

        return when (member.approval) {
            ApprovalStatus.APPROVED -> {
                val roles = rolesFor(member)
                val access = jwtUtil.createAccessToken(email, roles = roles)
                val refresh = jwtUtil.createRefreshToken(email, roles = roles)
                refreshTokenCacheUtil.saveRefreshToken(email, refresh, Duration.ofDays(7))

                ResponseEntity.ok()
                    .header("Authorization", "Bearer $access")
                    .header("Refresh-Token", refresh)
                    .body(mapOf("status" to "APPROVED"))
            }

            ApprovalStatus.PENDING -> {
                if (member.hasInfo) {
                    ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(
                            mapOf(
                                "status" to "PENDING",
                                "message" to "서비스 승인 대기중입니다. 1주일 이상 승인되지 않을 시 경영총괄팀으로 문의주세요."
                            )
                        )
                } else {
                    val onboarding = jwtUtil.createOnboardingToken(email, minutes = 30)
                    ResponseEntity.ok()
                        .header("Authorization", "Bearer $onboarding")
                        .body(
                            mapOf(
                                "status" to "PENDING",
                                "allowedEndpoint" to "/api/v1/members/onboarding",
                                "expiresInMinutes" to 30
                            )
                        )
                }
            }

            ApprovalStatus.REJECTED -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("status" to "REJECTED"))
            }
        }
    }

    override fun reissueAccessToken(refreshToken: String): String {
        if (jwtUtil.isExpired(refreshToken)) {
            throw CustomException(AuthErrorCode.EXPIRED_REFRESH_TOKEN)
        }

        val email = jwtUtil.getEmail(refreshToken)
        val stored = refreshTokenCacheUtil.getRefreshToken(email)
            ?: throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        if (stored != refreshToken) {
            throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }

        val member = memberService.getByEmail(email)
        if (member.approval != ApprovalStatus.APPROVED) {
            throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }

        val roles = rolesFor(member)
        return jwtUtil.createAccessToken(email, roles)
    }
}
