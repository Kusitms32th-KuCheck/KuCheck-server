package onku.backend.domain.session.service

import onku.backend.domain.attendance.AttendancePolicy.ABSENT_START_MINUTES
import onku.backend.domain.attendance.finalize.FinalizeEvent
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.repository.SessionDetailRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import onku.backend.domain.session.util.SessionTimeUtil.absentBoundary

@Service
class SessionDetailService(
    private val sessionDetailRepository: SessionDetailRepository,
    private val sessionRepository: SessionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun upsertSessionDetail(session : Session, upsertSessionDetailRequest: UpsertSessionDetailRequest): Long {
        val detail = if (upsertSessionDetailRequest.sessionDetailId != null) {
            val d = sessionDetailRepository.findById(upsertSessionDetailRequest.sessionDetailId)
                .orElseThrow { CustomException(SessionErrorCode.SESSION_DETAIL_NOT_FOUND) }
            d.place = upsertSessionDetailRequest.place
            d.startTime = upsertSessionDetailRequest.startTime
            d.endTime = upsertSessionDetailRequest.endTime
            d.content = upsertSessionDetailRequest.content
            d
        } else {
            sessionDetailRepository.save(
                SessionDetail(
                    place =  upsertSessionDetailRequest.place,
                    startTime =  upsertSessionDetailRequest.startTime,
                    endTime =  upsertSessionDetailRequest.endTime,
                    content =  upsertSessionDetailRequest.content
                )
            )
        }
        session.sessionDetail = detail

        sessionRepository.save(session)

        val runAt = absentBoundary(session, ABSENT_START_MINUTES)
        val sessionId = session.id ?: throw CustomException(SessionErrorCode.SESSION_NOT_FOUND)

        applicationEventPublisher.publishEvent(
            FinalizeEvent(sessionId, runAt)
        )

        return (detail.id ?: 0L)
    }

    @Transactional(readOnly = true)
    fun getById(id : Long) : SessionDetail {
        return sessionDetailRepository.findById(id).orElseThrow{
            CustomException(SessionErrorCode.SESSION_DETAIL_NOT_FOUND)
        }
    }
}