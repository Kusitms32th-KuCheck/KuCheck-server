package onku.backend.domain.absence.repository.projection

import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceType
import java.time.LocalDate
import java.time.LocalDateTime

interface GetMyAbsenceReportView {
    fun getAbsenceReportId(): Long
    fun getAbsenceType(): AbsenceType
    fun getAbsenceReportApproval(): AbsenceReportApproval
    fun getSubmitDateTime(): LocalDateTime
    fun getSessionTitle(): String
    fun getSessionStartDateTime(): LocalDate
}