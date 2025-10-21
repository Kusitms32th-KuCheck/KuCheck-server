package onku.backend.domain.session.service

import onku.backend.domain.session.validator.SessionValidator
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
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class SessionService(
    private val sessionRepository: SessionRepository,
    private val sessionValidator: SessionValidator,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Seoul"))
) {
    @Transactional(readOnly = true)
    fun getUpcomingSessionsForAbsence(pageable: Pageable): Page<SessionAboutAbsenceResponse> {
        val now = LocalDateTime.now(clock)

        val sessions = sessionRepository.findUpcomingSessions(now.toLocalDate(), pageable)

        return sessions.map { s ->
            val active = sessionValidator.isImminentSession(s, now)
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