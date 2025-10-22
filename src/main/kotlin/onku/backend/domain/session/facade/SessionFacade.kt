package onku.backend.domain.session.facade

import onku.backend.domain.member.Member
import onku.backend.domain.session.dto.request.DeleteSessionImageRequest
import onku.backend.domain.session.dto.response.SessionAboutAbsenceResponse
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.request.UploadSessionImageRequest
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.dto.response.GetInitialSessionResponse
import onku.backend.domain.session.dto.response.UploadSessionImageResponse
import onku.backend.domain.session.dto.response.UpsertSessionDetailResponse
import onku.backend.domain.session.service.SessionDetailService
import onku.backend.domain.session.service.SessionImageService
import onku.backend.domain.session.service.SessionService
import onku.backend.global.page.PageResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.service.S3Service
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class SessionFacade(
    private val sessionService: SessionService,
    private val sessionDetailService: SessionDetailService,
    private val sessionImageService : SessionImageService,
    private val s3Service: S3Service
) {
    fun showSessionAboutAbsence(page: Int, size: Int): PageResponse<SessionAboutAbsenceResponse> {
        val pageRequest = PageRequest.of(page, size)
        val sessionPage = sessionService.getUpcomingSessionsForAbsence(pageRequest)
        return PageResponse.from(sessionPage)
    }

    fun sessionSave(sessionSaveRequestList: List<SessionSaveRequest>): Boolean {
        return sessionService.saveAll(sessionSaveRequestList)
    }

    fun getInitialSession(page: Int, size: Int): PageResponse<GetInitialSessionResponse> {
        val pageRequest = PageRequest.of(page, size)
        val initialSessionPage = sessionService.getInitialSession(pageRequest)
        return PageResponse.from(initialSessionPage)
    }

    fun upsertSessionDetail(upsertSessionDetailRequest: UpsertSessionDetailRequest): UpsertSessionDetailResponse {
        val session = sessionService.getById(upsertSessionDetailRequest.sessionId)
        return UpsertSessionDetailResponse(
            sessionDetailService.upsertSessionDetail(
                session,
                upsertSessionDetailRequest
            )
        )
    }

    fun uploadSessionImage(
        member: Member,
        uploadSessionImageRequest: UploadSessionImageRequest
    ): List<UploadSessionImageResponse> {
        val sessionDetail = sessionDetailService.getById(uploadSessionImageRequest.sessionDetailId)

        val preSignedList = uploadSessionImageRequest.imageFileName.map { image ->
            val preSign = s3Service.getPostS3Url(
                member.id!!,
                image.fileName,
                FolderName.SESSION.name
            )
            preSign
        }
        val savedImages = sessionImageService.uploadImages(
            sessionDetail = sessionDetail,
            preSignedImages = preSignedList
        )

        return savedImages.map { entity ->
            val preSign = preSignedList.first { it.key == entity.url }
            UploadSessionImageResponse(
                sessionImageId = entity.id!!,
                sessionImagePreSignedUrl = preSign.preSignedUrl
            )
        }
    }

    fun deleteSessionImage(deleteSessionImageRequest: DeleteSessionImageRequest): GetPreSignedUrlDto {
        val sessionImage = sessionImageService.getById(deleteSessionImageRequest.sessionImageId)
        val getS3UrlDto = s3Service.getDeleteS3Url(sessionImage.url)
        sessionImageService.deleteImage(sessionImage.id!!)
        return GetPreSignedUrlDto(
            getS3UrlDto.preSignedUrl
        )
    }
}