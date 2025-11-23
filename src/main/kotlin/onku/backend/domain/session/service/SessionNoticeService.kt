package onku.backend.domain.session.service

import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionNoticeService(
    private val sessionRepository: SessionRepository,
    private val sessionImageRepository: SessionImageRepository,
) {
    @Transactional(readOnly = true)
    fun getSessionWithImages(sessionId: Long): Triple<Session, SessionDetail, List<SessionImage>> {
        val session = sessionRepository.findWithDetail(sessionId)
            ?: throw CustomException(SessionErrorCode.SESSION_NOT_FOUND)

        val detail = session.sessionDetail
            ?: throw CustomException(SessionErrorCode.SESSION_DETAIL_NOT_FOUND)

        val images = sessionImageRepository.findByDetailId(detail.id!!)

        return Triple(session, detail, images)
    }
}