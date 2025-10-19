package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotNull

data class UpdateKupportersPointsRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val kuportersPoints: Int?
)