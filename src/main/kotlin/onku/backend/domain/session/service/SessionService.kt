package onku.backend.domain.session.service

import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.session.validator.SessionValidator
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.dto.response.SessionAboutAbsenceResponse
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.response.GetInitialSessionResponse
import onku.backend.domain.session.dto.response.SessionCardInfo
import onku.backend.domain.session.dto.response.ThisWeekSessionInfo
import onku.backend.domain.session.repository.SessionDetailRepository
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.service.S3Service
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
    private val sessionImageRepository: SessionImageRepository,
    private val s3Service: S3Service,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Seoul")),
    private val attendanceRepository: AttendanceRepository,
    private val absenceReportRepository: AbsenceReportRepository,
    private val sessionDetailRepository: SessionDetailRepository,
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
        return sessionRepository.findByIdOrNull(id) ?: throw CustomException(SessionErrorCode.SESSION_NOT_FOUND)
    }

    @Transactional
    fun saveAll(requests: List<SessionSaveRequest>): Boolean {
        val sessions = requests.map { r ->
            Session(
                title = r.title,
                startDate = r.sessionDate,
                category = r.category,
                week = r.week,
                sessionDetail = null,
                isHoliday = r.isHoliday
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
                sessionDetailId = s.sessionDetail?.id,
                isHoliday = s.isHoliday
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
                endTime = it.endTime,
                isHoliday = it.isHoliday,
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
                    startDate = session.startDate,
                    isHoliday = session.isHoliday,
                )
            }
    }

    /**
     * - 해당 sessionId의 데이터가 Session 테이블에 존재하는지 확인
     * - [삭제] Session id를 FK로 가지는 attendance, absenceReport 레코드 먼저 삭제
     * - sessionDetail 존재 여부 확인 및 존재 시 detailId 조회
     * - [삭제] FK를 고려하여 detailId에 해당하는 이미지(SessionImage) 먼저 삭제 (S3 + DB)
     * - [삭제] detailId에 해당하는 sessionDetail 레코드 삭제
     * - [삭제] sessionId에 해당하는 Session 레코드 삭제
     */
    @Transactional
    fun deleteCascade(sessionId: Long) {
        sessionRepository.findWithDetail(sessionId)
            ?: throw CustomException(SessionErrorCode.SESSION_NOT_FOUND)

        attendanceRepository.deleteAllBySessionId(sessionId)
        absenceReportRepository.deleteAllBySessionId(sessionId)

        val detailId = sessionRepository.findDetailIdBySessionId(sessionId)
        if (detailId != null) {
            val keys = sessionImageRepository.findAllImageKeysByDetailId(detailId).filter { it.isNotBlank() }
            if (keys.isNotEmpty()) s3Service.deleteObjectsNow(keys)

            sessionImageRepository.deleteByDetailIdBulk(detailId)
            sessionRepository.detachDetailFromSession(sessionId)
            sessionDetailRepository.deleteById(detailId)
        }
        sessionRepository.deleteById(sessionId)
    }

    @Transactional(readOnly = true)
    fun getByDetailIdFetchDetail(sessionDetailId : Long) : Session {
        return sessionRepository.findByDetailIdFetchDetail(sessionDetailId)
            ?: throw CustomException(SessionErrorCode.SESSION_NOT_FOUND)
    }
}