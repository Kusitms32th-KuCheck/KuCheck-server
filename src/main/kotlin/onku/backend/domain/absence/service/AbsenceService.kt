package onku.backend.domain.absence.service

import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AbsenceService(
    private val absenceReportRepository: AbsenceReportRepository
) {
    @Transactional
    fun submitAbsenceReport(member: Member, submitAbsenceReportRequest: SubmitAbsenceReportRequest, fileKey : String, session : Session) {
        absenceReportRepository.save(
            AbsenceReport.createAbsenceReport(
                member = member,
                session = session,
                submitAbsenceReportRequest,
                fileKey
            )
        )
    }
}