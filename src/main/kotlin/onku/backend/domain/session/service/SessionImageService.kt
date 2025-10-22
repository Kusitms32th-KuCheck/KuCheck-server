package onku.backend.domain.session.service

import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import org.springframework.stereotype.Service

@Service
class SessionImageService(
    private val sessionImageRepository: SessionImageRepository,
) {
    fun uploadImage(key: String, sessionDetail: SessionDetail) {
        sessionImageRepository.save(
            SessionImage(
                sessionDetail = sessionDetail,
                url = key
            )
        )
    }
}