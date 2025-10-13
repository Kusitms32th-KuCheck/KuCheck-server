package onku.backend.global.redis.util

import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.Base64

@Component
class TokenGenerator {
    private val random = SecureRandom()
    fun generateOpaqueToken(bytes: Int = 32): String {
        val buf = ByteArray(bytes)
        random.nextBytes(buf)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf)
    }
}
