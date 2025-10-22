package onku.backend.domain.session.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import onku.backend.domain.session.enums.SessionCategory
import java.time.LocalDate

data class SessionSaveRequest (
    @field:NotNull val week : Long,
    @field:NotNull val sessionDate : LocalDate,
    @field:NotBlank val title : String,
    @field:NotNull val category: SessionCategory
    )