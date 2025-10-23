package onku.backend.domain.absence.dto.response

import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceType
import java.time.LocalDate
import java.time.LocalDateTime

data class GetMyAbsenceReportResponse(
    val absenceReportId : Long,
    val absenceType : AbsenceType,
    val absenceReportApproval : AbsenceReportApproval,
    val submitDateTime : LocalDateTime,
    val sessionTitle : String,
    val sessionStartDate: LocalDate
)
