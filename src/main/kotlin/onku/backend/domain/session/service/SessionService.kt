package onku.backend.domain.session.service

import onku.backend.domain.session.Session
import onku.backend.domain.session.dto.SessionAboutAbsenceResponse
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    fun getById(id : Long) : Session {
        return sessionRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.SESSION_NOT_FOUND)
    }
}