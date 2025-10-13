package onku.backend.global.redis.cache

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RefreshTokenCache(
    private val redisTemplate: RedisTemplate<String, String>
) {
    companion object {
        private const val PREFIX = "refresh_token:"
    }

    fun saveRefreshToken(email: String, refreshToken: String, ttl: Duration) {
        redisTemplate.opsForValue().set(PREFIX + email, refreshToken, ttl)
    }

    fun getRefreshToken(email: String): String? =
        redisTemplate.opsForValue().get(PREFIX + email)

    fun deleteRefreshToken(email: String) {
        redisTemplate.delete(PREFIX + email)
    }
}
