package onku.backend.global.auth.config

import onku.backend.domain.member.enums.Role
import onku.backend.global.auth.jwt.JwtFilter
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.config.CustomCorsConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil
) {
    companion object {
        private val ALLOWED_GET = arrayOf(
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/health",
            "/actuator/health",
            "/test/push/**"
        )
        private val ALLOWED_POST = arrayOf(
            "/api/v1/auth/kakao",
            "/api/v1/auth/reissue",
        )

        private val ONBOARDING_ENDPOINT = arrayOf(
            "/api/v1/members/onboarding/**",
            "/api/v1/members/profile/image/url"
        )

        private val STAFF_ENDPOINT = arrayOf( // 운영진
            "/api/v1/session/staff/**",
            "/api/v1/members/*/approval",
            "/api/v1/members/*/profile",
            "/api/v1/members/approvals",
            "/api/v1/members/requests",
            "/api/v1/notice/manage/**",
        )

        private val MANAGEMENT_ENDPOINT = arrayOf( // 경총
            "/api/v1/kupick/manage/**",
            "/api/v1/points/manage/**",
            "/api/v1/attendance/manage/**",
            "/api/v1/absence/manage/**"
        )

        private val EXECUTIVE = arrayOf( // 회장단
            "/api/v1/members/*/role",
        )
    }

    @Bean
    fun filterChain(http: HttpSecurity, corsConfiguration: CustomCorsConfig): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors{it.configurationSource(
                corsConfiguration.corsConfigurationSource()
            )}
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    // 공개 엔드포인트
                    .requestMatchers(*ALLOWED_GET).permitAll()
                    .requestMatchers(*ALLOWED_POST).permitAll()

                    // 권한 별 엔드포인트
                    .requestMatchers(*ONBOARDING_ENDPOINT).hasRole(Role.GUEST.name)
                    .requestMatchers(*STAFF_ENDPOINT).hasRole(Role.STAFF.name)
                    .requestMatchers(*MANAGEMENT_ENDPOINT).hasRole(Role.MANAGEMENT.name)
                    .requestMatchers(*EXECUTIVE).hasAnyRole(Role.EXECUTIVE.name)
                    .anyRequest().hasRole(Role.USER.name)
            }
            .addFilterBefore(JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}