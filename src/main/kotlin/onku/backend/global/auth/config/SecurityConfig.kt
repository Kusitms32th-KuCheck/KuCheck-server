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
        // TODO: 엔드포인트가 늘어나면 arrayOf()로 수정
        private const val ONBOARDING_ENDPOINT = "/api/v1/members/onboarding/**" // 온보딩
        private const val ADMIN_ENDPOINT = "/api/v1/auth/admin/**" // 운영진 전체
        private val MANAGEMENT_ENDPOINT = arrayOf(
            "/api/v1/attendance/scan/**",
            "/api/v1/kupick/manage/**") // 경총
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
                    .requestMatchers(ONBOARDING_ENDPOINT).hasRole(Role.GUEST.name)
                    .requestMatchers(ADMIN_ENDPOINT).hasRole(Role.ADMIN.name)
                    .requestMatchers(*MANAGEMENT_ENDPOINT).hasRole(Role.MANAGEMENT.name)
                    .anyRequest().hasRole(Role.USER.name)
            }
            .addFilterBefore(JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}