package onku.backend.domain.absence.facade

import onku.backend.domain.absence.dto.request.EstimateAbsenceReportRequest
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.dto.response.GetMemberAbsenceReportResponse
import onku.backend.domain.absence.dto.response.GetMyAbsenceReportResponse
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceSubmitType
import onku.backend.domain.absence.service.AbsenceService
import onku.backend.domain.session.validator.SessionValidator
import onku.backend.domain.member.Member
import onku.backend.global.alarm.enums.AlarmTitleType
import onku.backend.domain.point.service.MemberPointHistoryService
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.service.SessionService
import onku.backend.global.alarm.AlarmMessage
import onku.backend.global.alarm.FCMService
import onku.backend.global.alarm.enums.AlarmEmojiType
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class AbsenceFacade(
    private val absenceService : AbsenceService,
    private val s3Service: S3Service,
    private val sessionService: SessionService,
    private val sessionValidator: SessionValidator,
    private val fcmService: FCMService,
    private val memberPointHistoryService: MemberPointHistoryService
) {
    fun submitAbsenceReport(member: Member, submitAbsenceReportRequest: SubmitAbsenceReportRequest): GetPreSignedUrlDto {
        val session = sessionService.getById(submitAbsenceReportRequest.sessionId)
        //세션 검증
        when {
            sessionValidator.isPastSession(session) -> {
                throw CustomException(SessionErrorCode.SESSION_PAST)
            }
            !sessionValidator.isImminentSession(session) -> {
                throw CustomException(SessionErrorCode.SESSION_PAST)
            }
            sessionValidator.isRestSession(session) -> {
                throw CustomException(SessionErrorCode.INVALID_SESSION)
            }
        }
        val preSignedUrlDto = s3Service.getPostS3Url(member.id!!, submitAbsenceReportRequest.fileName, FolderName.ABSENCE.name, UploadOption.FILE)
        absenceService.submitAbsenceReport(member, submitAbsenceReportRequest, preSignedUrlDto.key, session)
        return GetPreSignedUrlDto(preSignedUrlDto.preSignedUrl)
    }

    fun getMyAbsenceReport(member: Member): List<GetMyAbsenceReportResponse> {
        val absenceReportList = absenceService.getMyAbsenceReports(member)
        val responses = absenceReportList.map { v ->
            GetMyAbsenceReportResponse(
                absenceReportId = v.absenceReportId,
                absenceType = v.absenceType,
                absenceReportApproval = v.absenceReportApproval,
                submitDateTime = v.submitDateTime,
                sessionTitle = v.sessionTitle,
                sessionStartDate = v.sessionStartDate
            )
        }
        return responses
    }

    fun getMemberAbsenceReport(sessionId: Long): List<GetMemberAbsenceReportResponse> {
        val absenceReports = absenceService.getBySessionId(sessionId)
        return absenceReports.map { report ->
            val member = report.member
            val memberProfile = member.memberProfile!!
            val submitDate = report.updatedAt.toLocalDate()
            val time = when (report.submitType) {
                AbsenceSubmitType.LATE -> report.lateDateTime?.toLocalTime()
                AbsenceSubmitType.EARLY_LEAVE -> report.leaveDateTime?.toLocalTime()
                else -> null
            }
            val preSignedUrl = report.url
                ?.takeIf { it.isNotBlank() } // null이 날 일은 없지만 혹시나 null이 날 경우를 대비(만약 그런 경우가 있으면 학회원 전체에 영향이 감)
                ?.let { key -> s3Service.getGetS3Url(0L, key).preSignedUrl }

            GetMemberAbsenceReportResponse(
                name = memberProfile.name ?: "UNKNOWN",
                part = memberProfile.part,
                absenceReportId = report.id!!,
                submitDate = submitDate,
                submitType = report.submitType,
                time = time,
                reason = report.reason,
                url = preSignedUrl,
                absenceApprovedType = when (report.approval) {
                    AbsenceReportApproval.SUBMIT -> null
                    AbsenceReportApproval.APPROVED -> report.approvedType
                }
            )
        }
    }

    @Transactional
    fun estimateAbsenceReport(absenceReportId: Long, estimateAbsenceReportRequest: EstimateAbsenceReportRequest): Boolean {
        val absenceReport = absenceService.getById(absenceReportId)
        absenceReport.updateApprovedType(estimateAbsenceReportRequest.approvedType)
        absenceReport.updateApproval(AbsenceReportApproval.APPROVED)
        memberPointHistoryService.upsertPointFromAbsenceReport(absenceReport)
        val now = LocalDateTime.now()
        fcmService.sendMessageTo(absenceReport.member, AlarmTitleType.ABSENCE_REPORT, AlarmEmojiType.WARNING, AlarmMessage.absenceReport(now.month.value, now.dayOfMonth, estimateAbsenceReportRequest.approvedType), null)
        return true
    }

}