package onku.backend.domain.attendance.finalize

import java.time.LocalDateTime

/**
 * - sessionId: 마감할 세션 PK
 * - runAt   : 결석 판정 경계 시각(= 이때 finalize 실행)
 */
data class FinalizeScheduleEvent(
    val sessionId: Long,
    val runAt: LocalDateTime
)
