package onku.backend.domain.absence.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import onku.backend.domain.absence.enums.AbsenceType

data class SubmitAbsenceReportRequest(
    val absenceReportId : Long?,
    @field:NotNull val sessionId : Long,
    val absenceType : AbsenceType,
    val reason : String,
    @field:NotBlank val fileName : String
)