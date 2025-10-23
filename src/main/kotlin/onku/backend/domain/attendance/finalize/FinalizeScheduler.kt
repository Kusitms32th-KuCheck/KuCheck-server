package onku.backend.domain.attendance.finalize

import onku.backend.domain.attendance.service.AttendanceFinalizeService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDateTime

@Service
class FinalizeScheduler(
    @Qualifier("AttendanceTaskScheduler")
    private val taskScheduler: TaskScheduler,
    private val finalizeService: AttendanceFinalizeService,
    private val clock: Clock
) {
    fun scheduleOnce(sessionId: Long, runAt: LocalDateTime) {
        val instant = runAt.atZone(clock.zone).toInstant()
        taskScheduler.schedule({ finalizeService.finalizeSession(sessionId) }, instant)
    }
}
