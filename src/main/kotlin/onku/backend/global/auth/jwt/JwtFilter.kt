package onku.backend.global.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val auth = request.getHeader("Authorization")
        if (auth.isNullOrBlank() || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); return
        }

        val token = auth.substringAfter(" ").trim()
        if (jwtUtil.isExpired(token)) {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            return
        }

        val email = jwtUtil.getEmail(token)
        val roles = jwtUtil.getRoles(token)

        val authorities = buildList {
            roles.forEach { add(SimpleGrantedAuthority("ROLE_${it.uppercase()}")) }
        }

        val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
        SecurityContextHolder.getContext().authentication = authentication
        filterChain.doFilter(request, response)
    }
}
