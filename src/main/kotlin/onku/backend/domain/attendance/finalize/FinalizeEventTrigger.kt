package onku.backend.domain.attendance.finalize

import onku.backend.domain.attendance.dto.FinalizeScheduleEvent
import onku.backend.domain.attendance.service.AttendanceFinalizeService
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class SessionFinalizeScheduleListener(
    private val finalizeScheduler: FinalizeScheduler,
    private val clock: Clock,
    private val attendanceFinalizeService: AttendanceFinalizeService
) {
    @org.springframework.transaction.event.TransactionalEventListener(
        classes = [FinalizeScheduleEvent::class],
        phase = org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT
    )
    fun onSessionFinalizeSchedule(event: FinalizeScheduleEvent) {
        val now = LocalDateTime.now(clock)

        if (!event.runAt.isAfter(now)) {
            runCatching { attendanceFinalizeService.finalizeSession(event.sessionId) }
            return
        }
        finalizeScheduler.schedule(event.sessionId, event.runAt)
    }
}
