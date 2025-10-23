package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class UpdateMemoRequest(
    @field:NotNull val memberId: Long?,
    @field:NotBlank val memo: String?
)