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
    private val registry = java.util.concurrent.ConcurrentHashMap<Long, java.util.concurrent.ScheduledFuture<*>>()

    fun schedule(sessionId: Long, runAt: LocalDateTime) {
        registry.remove(sessionId)?.cancel(false) // 기존 예약이 있으면 취소

        val instant = runAt.atZone(clock.zone).toInstant()
        val future = taskScheduler.schedule(
            { finalizeService.finalizeSession(sessionId) },
            instant
        )
        if (future != null) registry[sessionId] = future
    }
}
