package onku.backend.domain.session.service

import onku.backend.domain.session.dto.SessionAboutAbsenceResponse
import onku.backend.domain.session.repository.SessionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {
    fun getUpcomingSessionsForAbsence(pageable: Pageable): Page<SessionAboutAbsenceResponse> {
        val zone = ZoneId.of("Asia/Seoul")
        val now = LocalDateTime.now(zone)
        val today = now.toLocalDate()

        val isFriOrSat = today.dayOfWeek == DayOfWeek.FRIDAY || today.dayOfWeek == DayOfWeek.SATURDAY
        val upcomingSaturday = if (isFriOrSat)
            today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        else null

        val sessions = sessionRepository.findUpcomingSessions(now, pageable)

        return sessions.map { s ->
            val sessionDate = s.startTime.atZone(zone).toLocalDate()
            val active = !(isFriOrSat && upcomingSaturday != null && sessionDate == upcomingSaturday)

            SessionAboutAbsenceResponse(
                sessionId = s.id,
                title = s.title,
                week = s.week,
                active = active
            )
        }
    }
}