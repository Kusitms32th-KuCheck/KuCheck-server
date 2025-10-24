package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

sealed interface UpdateManualPointRequest

data class ToggleMemberRequest(
    @field:NotNull val memberId: Long? = null
)

data class UpdateKupportersPointsRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val kuportersPoints: Int?
) : UpdateManualPointRequest

data class UpdateMemoRequest(
    @field:NotNull val memberId: Long?,
    @field:NotBlank val memo: String?
) : UpdateManualPointRequest

data class UpdateStudyPointsRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val studyPoints: Int?
) : UpdateManualPointRequest
