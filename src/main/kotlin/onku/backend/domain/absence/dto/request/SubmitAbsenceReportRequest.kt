package onku.backend.domain.absence.dto.request

import jakarta.validation.constraints.NotNull
import onku.backend.domain.absence.dto.annotation.ValidAbsenceReport
import onku.backend.domain.absence.enums.AbsenceSubmitType
import java.time.LocalDateTime
@ValidAbsenceReport
data class SubmitAbsenceReportRequest(
    val absenceReportId : Long?,
    @field:NotNull val sessionId : Long,
    val submitType : AbsenceSubmitType,
    val reason : String,
    val fileName : String,
    val lateDateTime : LocalDateTime?,
    val leaveDateTime : LocalDateTime?
)