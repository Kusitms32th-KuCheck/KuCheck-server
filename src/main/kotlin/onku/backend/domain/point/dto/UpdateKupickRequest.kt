package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotNull

data class UpdateKupickRequest(
    @field:NotNull
    val memberId: Long?,
    @field:NotNull
    val isKupick: Boolean?
)