package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

sealed interface UpdateManualPointRequest

data class UpdateIsTfRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val isTf: Boolean?
) : UpdateManualPointRequest

data class UpdateIsStaffRequest(
    @field:NotNull val memberId: Long? = null,
    @field:NotNull val isStaff: Boolean? = null
)

data class UpdateKupickRequest(
    @field:NotNull val memberId: Long?,
    @field:NotNull val isKupick: Boolean?
) : UpdateManualPointRequest

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
