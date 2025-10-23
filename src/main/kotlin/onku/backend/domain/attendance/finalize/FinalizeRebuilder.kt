package onku.backend.domain.attendance.finalize

import onku.backend.domain.attendance.AttendancePolicy
import onku.backend.domain.attendance.service.AttendanceFinalizeService
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.util.SessionTimeUtil
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime

@Component
class FinalizeRebuilder(
    private val sessionRepository: SessionRepository,
    private val oneShot: FinalizeScheduler,
    private val attendanceFinalizeService: AttendanceFinalizeService,
    private val clock: Clock
) {
    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent::class) // 서버 실행 직후 1회 실행
    fun rebuild() {
        val now = LocalDateTime.now(clock)
        val pivot = now.minusMinutes(AttendancePolicy.ABSENT_START_MINUTES)

        // 이미 결석 판정 시각을 지난 세션 → 즉시 finalize
        sessionRepository.findFinalizeDue(pivot).forEach { s ->
            runCatching { attendanceFinalizeService.finalizeSession(s.id!!) }
        }

        // 아직 결석 판정 시각이 지나지 않은 세션 → 경계 시각으로 재예약
        sessionRepository.findUnfinalizedAfter(pivot).forEach { s ->
            runCatching {
                val runAt = SessionTimeUtil.absentBoundary(s, AttendancePolicy.ABSENT_START_MINUTES)
                oneShot.scheduleOnce(s.id!!, runAt)
            }
        }
    }
}
