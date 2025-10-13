package onku.backend.global.redis

import onku.backend.global.redis.dto.TokenData
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.dao.DataAccessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class AttendanceTokenCacheUtil(
    private val redis: StringRedisTemplate,
    @Qualifier("attendanceSwapScript")
    private val swapScript: DefaultRedisScript<String>,
    @Qualifier("attendanceConsumeScript")
    private val consumeScript: DefaultRedisScript<String>
) {
    private val zone: ZoneId = ZoneId.of("UTC")

    private companion object {
        private const val TOKEN_PREFIX = "attendance:token:"
        private const val MEMBER_PREFIX = "attendance:member:"
        private const val ACTIVE_SUFFIX = ":active"

        fun tokenKey(token: String) = "$TOKEN_PREFIX$token"
        fun memberKey(memberId: Long) = "$MEMBER_PREFIX$memberId$ACTIVE_SUFFIX"
    }

    private fun <T> execScript(
        script: DefaultRedisScript<T>,
        keys: List<String>,
        vararg args: String
    ): T? = try {
        redis.execute(script, keys, *args)
    } catch (e: DataAccessException) {
        null
    }

    /**
     * 새 토큰을 발급하고 활성 토큰을 교체
     * 이전 활성 토큰이 있으면 used=1 + TTL 상한(usedGraceSeconds)으로 수정해서 active token은 단일이도록 보장
     */
    fun putAsActiveSingle(
        memberId: Long,
        token: String,
        issuedAt: LocalDateTime,
        expAt: LocalDateTime,
        ttlSeconds: Long,
        usedGraceSeconds: Long? = null
    ) {
        val payload = TokenData(memberId, issuedAt, expAt, used = false).toPayload(zone)
        val keys = listOf(
            memberKey(memberId),     // KEYS[1]
            TOKEN_PREFIX,            // KEYS[2]
            tokenKey(token)          // KEYS[3]
        )
        val baseArgs = mutableListOf(
            payload,                 // ARGV[1]
            ttlSeconds.toString(),   // ARGV[2]
            token                    // ARGV[3]
        )
        usedGraceSeconds?.let { baseArgs.add(it.toString()) }

        execScript(swapScript, keys, *baseArgs.toTypedArray())
            ?: error("Failed to execute swapScript for token=$token")
    }

    /**
     * 토큰을 소비(출석체크 성공)하면서 used=1로 수정하고 TTL을 상한(usedGraceSeconds) 이하로 수정
     */
    fun consumeToken(token: String, usedGraceSeconds: Long = 30): TokenData? {
        val keys = listOf(tokenKey(token))
        val raw: String? = execScript(consumeScript, keys, usedGraceSeconds.toString())
        return TokenData.parse(raw, zone)
    }

    /** 멤버의 현재 활성 토큰 문자열을 조회 */
    fun getActiveTokenOf(memberId: Long): String? =
        redis.opsForValue().get(memberKey(memberId))

    /** 토큰 payload 확인 → 해당 member가 이미 세션에 출석했는지 여부를 검사하기 위해 */
    fun peek(token: String): TokenData? =
        TokenData.parse(redis.opsForValue().get(tokenKey(token)), zone)
}
