package onku.backend.global.auth.config

import onku.backend.global.auth.jwt.JwtFilter
import onku.backend.global.auth.jwt.JwtUtil
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
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
        )
        private val ALLOWED_POST = arrayOf(
            "/api/v1/auth/kakao",
            "/api/v1/auth/reissue",
        )
        private const val ONBOARDING_ENDPOINT = "/api/v1/members/onboarding"
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it
                    // 공개 엔드포인트
                    .requestMatchers(*ALLOWED_GET).permitAll()
                    .requestMatchers(*ALLOWED_POST).permitAll()

                    // 권한 별 엔드포인트
                    // 온보딩
                    .requestMatchers(ONBOARDING_ENDPOINT).hasAuthority("ONBOARDING_ONLY")

                    // 관리자
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                    .anyRequest().hasRole("USER")
            }
            .addFilterBefore(JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
