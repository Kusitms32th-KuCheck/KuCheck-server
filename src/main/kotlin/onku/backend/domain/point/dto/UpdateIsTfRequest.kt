package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotNull

data class UpdateIsTfRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val isTf: Boolean?
)