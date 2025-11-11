package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

sealed interface UpdateManualPointRequest

data class ToggleMemberRequest(
    @field:NotNull
    val memberId: Long,

    @field:NotBlank
    @field:Pattern(regexp = """^\d{4}-(0[1-9]|1[0-2])$""", message = "yearMonth는 yyyy-MM 형식이어야 합니다.")
    val yearMonth: String
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
