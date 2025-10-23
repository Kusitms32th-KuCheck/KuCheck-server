package onku.backend.domain.session.service

import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.SessionImage
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.dto.GetS3UrlDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SessionImageService(
    private val sessionImageRepository: SessionImageRepository,
) {
    @Transactional
    fun uploadImages(
        sessionDetail: SessionDetail,
        preSignedImages: List<GetS3UrlDto>
    ): List<SessionImage> {
        val entities = preSignedImages.map { info ->
            SessionImage(
                sessionDetail = sessionDetail,
                url = info.key
            )
        }
        return sessionImageRepository.saveAll(entities).toList()
    }

    @Transactional
    fun deleteImage(id : Long) {
        return sessionImageRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getById(id : Long): SessionImage {
        return sessionImageRepository.findById(id).orElseThrow{CustomException(SessionErrorCode.SESSION_IMAGE_NOT_FOUND)}
    }

    @Transactional(readOnly = true)
    fun findAllBySessionDetailId(detailId : Long) : List<SessionImage> {
        return sessionImageRepository.findAllBySessionDetailId(detailId)
    }
}