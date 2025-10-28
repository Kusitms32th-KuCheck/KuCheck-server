package onku.backend.domain.absence.service

import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.dto.response.GetMyAbsenceReportResponse
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AbsenceService(
    private val absenceReportRepository: AbsenceReportRepository,

) {
    @Transactional
    fun submitAbsenceReport(member: Member, submitAbsenceReportRequest: SubmitAbsenceReportRequest, fileKey : String, session : Session) {
        val existingReport = submitAbsenceReportRequest.absenceReportId?.let {
            absenceReportRepository.findByIdOrNull(it)
        }
        val report = if (existingReport != null) {
            existingReport.updateAbsenceReport(submitAbsenceReportRequest, fileKey, session)
            existingReport
        } else {
            AbsenceReport.createAbsenceReport(
                member = member,
                session = session,
                submitAbsenceReportRequest,
                fileKey
            )
        }
        absenceReportRepository.save(report)
    }

    @Transactional(readOnly = true)
    fun getMyAbsenceReports(member: Member): List<GetMyAbsenceReportResponse> {
        val absenceReports = absenceReportRepository.findMyAbsenceReports(member)
        return absenceReports.map { a ->
            GetMyAbsenceReportResponse(
                absenceReportId = a.getAbsenceReportId(),
                absenceType = a.getAbsenceType(),
                absenceReportApproval = a.getAbsenceReportApproval(),
                submitDateTime = a.getSubmitDateTime(),
                sessionTitle = a.getSessionTitle(),
                sessionStartDate = a.getSessionStartDateTime()
            )
        }
    }
}