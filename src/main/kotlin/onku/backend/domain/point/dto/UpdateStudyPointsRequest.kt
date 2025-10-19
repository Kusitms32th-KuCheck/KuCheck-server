package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotNull

data class UpdateStudyPointsRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val studyPoints: Int?
)