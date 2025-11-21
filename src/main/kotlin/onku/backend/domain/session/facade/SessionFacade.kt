package onku.backend.domain.session.facade

import onku.backend.domain.member.Member
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.dto.SessionImageDto
import onku.backend.domain.session.dto.request.DeleteSessionImageRequest
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.request.UploadSessionImageRequest
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.dto.response.*
import onku.backend.domain.session.service.SessionDetailService
import onku.backend.domain.session.service.SessionImageService
import onku.backend.domain.session.service.SessionNoticeService
import onku.backend.domain.session.service.SessionService
import onku.backend.global.exception.CustomException
import onku.backend.global.page.PageResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class SessionFacade(
    private val sessionService: SessionService,
    private val sessionDetailService: SessionDetailService,
    private val sessionImageService : SessionImageService,
    private val s3Service: S3Service,
    private val sessionNoticeService: SessionNoticeService,
    private val clock: Clock = Clock.system(ZoneId.of("Asia/Seoul")),
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
        val images = sessionImageService.findAllBySessionDetailId(detailId)
        val session = sessionService.getByDetailIdFetchDetail(detailId)
        val detail = session.sessionDetail!!
        val imageDtos = images.map { image ->
            val preSignedDto = s3Service.getGetS3Url(
                memberId = 0,
                key = image.url
            )

            SessionImageDto(
                sessionImageId = image.id!!,
                sessionImagePreSignedUrl = preSignedDto.preSignedUrl,
                sessionOriginalFileName = preSignedDto.originalName
            )
        }

        return GetDetailSessionResponse(
            sessionId = session.id!!,
            sessionDetailId = detail.id!!,
            place = detail.place,
            startTime = detail.startTime,
            endTime = detail.endTime,
            content = detail.content,
            sessionImages = imageDtos,
            createdAt = detail.createdAt,
            updatedAt = detail.updatedAt
        )
    }

    fun showThisWeekSessionInfo(): List<ThisWeekSessionInfo> {
        return sessionService.getThisWeekSession()
    }

    fun showAllSessionCards(): List<SessionCardInfo> {
        return sessionService.getAllSessionsOrderByStartDate()
    }

    fun getSessionNotice(sessionId: Long): GetSessionNoticeResponse {
        val (session, detail, images) = sessionNoticeService.getSessionWithImages(sessionId)

        // Presigned URL 생성
        val imageDtos = images.map { img ->
            val preSignDto = s3Service.getGetS3Url(0L, img.url)
            SessionImageDto(
                sessionImageId = img.id!!,
                sessionImagePreSignedUrl = preSignDto.preSignedUrl,
                sessionOriginalFileName = preSignDto.originalName
            )
        }

        return GetSessionNoticeResponse(
            sessionId = session.id!!,
            sessionDetailId = detail.id!!,
            title = session.title,
            place = detail.place,
            startDate = session.startDate,
            startTime = detail.startTime,
            endTime = detail.endTime,
            content = detail.content,
            images = imageDtos,
            isHoliday = session.isHoliday,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt
        )
    }
    fun deleteSession(sessionId: Long) {
        sessionService.deleteCascade(sessionId)
    }

    @Transactional
    fun patchSession(id: Long, sessionSaveRequest: SessionSaveRequest): Boolean {
        val session = sessionService.getById(id)
        if(session.startDate.isBefore(LocalDate.now())) {
            throw CustomException(SessionErrorCode.SESSION_PAST)
        }
        session.update(sessionSaveRequest)
        return true
    }

    @Transactional
    fun resetSessionTime(sessionId: Long): SessionTimeResetResponse {
        val session = sessionService.getById(sessionId)

        session.attendanceFinalized = false
        session.attendanceFinalizedAt = null

        val detail = session.sessionDetail
            ?: throw CustomException(SessionErrorCode.SESSION_DETAIL_NOT_FOUND)

        val baseDateTime = LocalDateTime.now(clock).plusMinutes(20)
        val newStartTime = baseDateTime.toLocalTime()
        val newEndTime = baseDateTime.plusHours(2).toLocalTime()

        detail.startTime = newStartTime
        detail.endTime = newEndTime

        return SessionTimeResetResponse(
            sessionId = session.id!!,
            startTime = newStartTime,
            endTime = newEndTime,
            attendanceFinalized = session.attendanceFinalized,
            attendanceFinalizedAt = session.attendanceFinalizedAt
        )
    }
}