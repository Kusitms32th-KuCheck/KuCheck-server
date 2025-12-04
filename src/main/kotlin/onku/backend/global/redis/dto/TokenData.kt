package onku.backend.global.redis.dto

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class TokenData(
    val memberId: Long,
    val issuedAt: LocalDateTime,
    val expAt: LocalDateTime,
    val used: Boolean
) {
    // TokenData 객체를 문자열로 직렬화해서 Redis에 저장
    fun toPayload(zone: ZoneId = ZoneId.of("UTC")): String {
        val issuedMs = issuedAt.atZone(zone).toInstant().toEpochMilli()
        val expMs = expAt.atZone(zone).toInstant().toEpochMilli()
        val usedFlag = if (used) 1 else 0
        return "$memberId|$issuedMs|$expMs|$usedFlag"
    }

    companion object {
        // redis에서 꺼낸 값을 TokenData 객체로 변경
        fun parse(raw: String?, zone: ZoneId = ZoneId.of("UTC")): TokenData? = runCatching {
            val s = raw?.trim().orEmpty()
            if (s.isEmpty()) return null
            val p = s.split('|')
            if (p.size != 4) return null

            val memberId = p[0].toLong()
            val issuedMs = p[1].toLong()
            val expMs = p[2].toLong()
            val used = p[3] == "1"

            val issuedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(issuedMs), zone)
            val expAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(expMs), zone)
            TokenData(memberId, issuedAt, expAt, used)
        }.getOrNull()
    }
}
