package onku.backend.domain.session.facade

import onku.backend.domain.member.Member
import onku.backend.domain.session.dto.SessionImageDto
import onku.backend.domain.session.dto.request.DeleteSessionImageRequest
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.request.UploadSessionImageRequest
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.dto.response.*
import onku.backend.domain.session.service.SessionDetailService
import onku.backend.domain.session.service.SessionImageService
import onku.backend.domain.session.service.SessionService
import onku.backend.global.page.PageResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
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
    fun showSessionAboutAbsence(): List<SessionAboutAbsenceResponse> {
        return sessionService.getUpcomingSessionsForAbsence()
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
                FolderName.SESSION.name,
                UploadOption.IMAGE
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

    fun getSessionDetailPage(detailId: Long): GetDetailSessionResponse {
        val detail = sessionDetailService.getById(detailId)
        val images = sessionImageService.findAllBySessionDetailId(detailId)

        val imageDtos = images.map { image ->
            val preSignedUrl = s3Service.getGetS3Url(
                memberId = 0,
                key = image.url
            ).preSignedUrl

            SessionImageDto(
                sessionImageId = image.id!!,
                sessionImagePreSignedUrl = preSignedUrl
            )
        }

        return GetDetailSessionResponse(
            sessionDetailId = detail.id!!,
            place = detail.place,
            startTime = detail.startTime,
            endTime = detail.endTime,
            content = detail.content,
            sessionImages = imageDtos
        )
    }

    fun showThisWeekSessionInfo(): List<ThisWeekSessionInfo> {
        return sessionService.getThisWeekSession()
    }
}