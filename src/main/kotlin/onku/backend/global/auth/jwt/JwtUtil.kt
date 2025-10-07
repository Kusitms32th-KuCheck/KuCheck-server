package onku.backend.global.auth.jwt

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.access-ttl:30m}") private val accessTtl: Duration,
    @Value("\${jwt.refresh-ttl:7d}") private val refreshTtl: Duration
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    private fun stripBearerPrefix(token: String?): String {
        require(!token.isNullOrBlank()) { "Token cannot be null or empty" }
        val t = token.trim()
        return if (t.startsWith("Bearer ", ignoreCase = true)) t.substring(7).trim() else t
    }

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(stripBearerPrefix(token)).payload

    fun getEmail(token: String): String = parseClaims(token).get("email", String::class.java)
    fun getRoles(token: String): List<String> = parseClaims(token).get("roles", List::class.java)?.map { it.toString() } ?: emptyList()
    fun getScopes(token: String): List<String> = parseClaims(token).get("scopes", List::class.java)?.map { it.toString() } ?: emptyList()

    fun isExpired(token: String): Boolean =
        try { parseClaims(token).expiration?.before(Date()) ?: true }
        catch (_: ExpiredJwtException) { true }

    fun createAccessToken(email: String, roles: List<String> = listOf("USER")): String =
        createJwt(email, roles, scopes = emptyList(), expiredMs = accessTtl.toMillis())

    fun createRefreshToken(email: String, roles: List<String> = listOf("USER")): String =
        createJwt(email, roles, scopes = emptyList(), expiredMs = refreshTtl.toMillis())

    fun createOnboardingToken(email: String, minutes: Long = 30): String =
        createJwt(email, roles = listOf("GUEST"), scopes = listOf("ONBOARDING_ONLY"), expiredMs = Duration.ofMinutes(minutes).toMillis())

    private fun createJwt(email: String, roles: List<String>, scopes: List<String>, expiredMs: Long): String {
        val now = Date()
        val exp = Date(now.time + expiredMs)
        return Jwts.builder()
            .claims(mapOf("email" to email, "roles" to roles, "scopes" to scopes))
            .issuedAt(now)
            .expiration(exp)
            .signWith(key)
            .compact()
    }
}
