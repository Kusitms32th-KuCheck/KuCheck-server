package onku.backend.domain.session.service

import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionImageService(
    private val sessionImageRepository: SessionImageRepository,
) {
    fun uploadImage(key: String, sessionDetail: SessionDetail) : SessionImage {
        return sessionImageRepository.save(
            SessionImage(
                sessionDetail = sessionDetail,
                url = key
            )
        )
    }

    @Transactional
    fun deleteImage(id : Long) {
        return sessionImageRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id : Long): SessionImage {
        return sessionImageRepository.findById(id).orElseThrow{CustomException(SessionErrorCode.SESSION_IMAGE_NOT_FOUND)}
    }
}