package onku.backend.global.auth.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.service.MemberService
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.dto.*
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.exception.CustomException
import onku.backend.global.redis.RefreshTokenCacheUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration

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
    override fun kakaoLogin(dto: KakaoLoginRequest): Pair<AuthLoginResult, AuthHeaders> {
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

                AuthLoginResult(
                    status = ApprovalStatus.APPROVED,
                    memberId = member.id,
                    role = member.role.name
                ) to AuthHeaders(
                    accessToken = access,
                    refreshToken = refresh
                )
            }

            ApprovalStatus.PENDING -> {
                if (member.hasInfo) {
                    AuthLoginResult(
                        status = ApprovalStatus.PENDING,
                    ) to AuthHeaders()
                } else {
                    val onboarding = jwtUtil.createOnboardingToken(email, minutes = 30)
                    AuthLoginResult(
                        status = ApprovalStatus.PENDING,
                    ) to AuthHeaders(
                        onboardingToken = onboarding
                    )
                }
            }

            ApprovalStatus.REJECTED -> {
                AuthLoginResult(
                    status = ApprovalStatus.REJECTED
                ) to AuthHeaders()
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

        if (stored != refreshToken) throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        val member = memberService.getByEmail(email)
        if (member.approval != ApprovalStatus.APPROVED) {
            throw CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }

        val roles = rolesFor(member)
        return jwtUtil.createAccessToken(email, roles)
    }
}
