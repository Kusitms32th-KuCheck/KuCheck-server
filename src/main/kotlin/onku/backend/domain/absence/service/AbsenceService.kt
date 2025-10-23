package onku.backend.domain.absence.service

import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.dto.response.GetMyAbsenceReportResponse
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
    fun getMyAbsenceReports(member: Member, pageable: Pageable): Page<GetMyAbsenceReportResponse> {
        val page = absenceReportRepository.findMyAbsenceReports(member, pageable)
        return page.map { v ->
            GetMyAbsenceReportResponse(
                absenceReportId = v.getAbsenceReportId(),
                absenceType = v.getAbsenceType(),
                absenceReportApproval = v.getAbsenceReportApproval(),
                submitDateTime = v.getSubmitDateTime(),
                sessionTitle = v.getSessionTitle(),
                sessionStartDate = v.getSessionStartDateTime()
            )
        }
    }
}