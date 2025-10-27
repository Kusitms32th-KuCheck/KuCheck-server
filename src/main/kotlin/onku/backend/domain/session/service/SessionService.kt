package onku.backend.domain.session.service

import onku.backend.domain.session.validator.SessionValidator
import onku.backend.domain.session.Session
import onku.backend.domain.session.dto.response.SessionAboutAbsenceResponse
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.response.GetInitialSessionResponse
import onku.backend.domain.session.dto.response.SessionCardInfo
import onku.backend.domain.session.dto.response.ThisWeekSessionInfo
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.time.TimeRangeUtil
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
    fun getUpcomingSessionsForAbsence(): List<SessionAboutAbsenceResponse> {
        val now = LocalDateTime.now(clock)

        val sessions = sessionRepository.findUpcomingSessions(now.toLocalDate())

        return sessions.map { s ->
            val active = sessionValidator.isImminentSession(s, now)
            SessionAboutAbsenceResponse(
                sessionId = s.id,
                title = s.title,
                week = s.week,
                startDate = s.startDate,
                active = active
            )
        }
    }

    @Transactional(readOnly = true)
    fun getById(id : Long) : Session {
        return sessionRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.SESSION_NOT_FOUND)
    }

    @Transactional
    fun saveAll(requests: List<SessionSaveRequest>): Boolean {
        val sessions = requests.map { r ->
            Session(
                title = r.title,
                startDate = r.sessionDate,
                category = r.category,
                week = r.week,
                sessionDetail = null
            )
        }
        sessionRepository.saveAll(sessions)
        return true
    }

    @Transactional(readOnly = true)
    fun getInitialSession(pageable: Pageable) : Page<GetInitialSessionResponse> {
        val initialSessions = sessionRepository.findAll(pageable)
        return initialSessions.map { s ->
            GetInitialSessionResponse(
                sessionId = s.id!!,
                startDate = s.startDate,
                title = s.title,
                category = s.category,
                sessionDetailId = s.sessionDetail?.id
            )
        }
    }

    @Transactional(readOnly = true)
    fun getThisWeekSession(): List<ThisWeekSessionInfo> {
        val range = TimeRangeUtil.thisWeekRange()
        return sessionRepository.findThisWeekSunToSat(range.startOfWeek, range.endOfWeek).map {
            ThisWeekSessionInfo(
                sessionId = it.sessionId,
                sessionDetailId = it.sessionDetailId,
                title = it.title,
                place = it.place,
                startDate = it.startDate,
                startTime = it.startTime,
                endTime = it.endTime
            )
        }
    }

    fun getAllSessionsOrderByStartDate(): List<SessionCardInfo> {
        return sessionRepository.findAllSessionsOrderByStartDate()
            .map { session ->
                SessionCardInfo(
                    sessionId = session.id!!,
                    sessionCategory = session.category,
                    title = session.title,
                    startDate = session.startDate
                )
            }
    }
}