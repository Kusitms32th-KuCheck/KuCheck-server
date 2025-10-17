package onku.backend.domain.absence.facade

import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.service.AbsenceService
import onku.backend.domain.absence.validator.AbsenceValidator
import onku.backend.domain.member.Member
import onku.backend.domain.session.service.SessionService
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.service.S3Service
import org.springframework.stereotype.Component

@Component
class AbsenceFacade(
    private val absenceService : AbsenceService,
    private val s3Service: S3Service,
    private val sessionService: SessionService,
    private val absenceValidator: AbsenceValidator
) {
    fun submitAbsenceReport(member: Member, submitAbsenceReportRequest: SubmitAbsenceReportRequest): GetPreSignedUrlDto {
        val session = sessionService.getById(submitAbsenceReportRequest.sessionId)
        when {
            absenceValidator.isPastSession(session) -> {
                throw CustomException(ErrorCode.SESSION_PAST)
            }
            absenceValidator.isImminentSession(session) -> {
                throw CustomException(ErrorCode.SESSION_IMMINENT)
            }
        }
        val preSignedUrlDto = s3Service.getPostS3Url(member.id!!, submitAbsenceReportRequest.fileName, FolderName.ABSENCE.name)
        absenceService.submitAbsenceReport(member, submitAbsenceReportRequest, preSignedUrlDto.key, session)
        return GetPreSignedUrlDto(preSignedUrlDto.preSignedUrl)
    }

}