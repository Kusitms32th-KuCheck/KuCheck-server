package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.member.enums.Role
import software.amazon.awssdk.annotations.NotNull

data class UpdateRoleRequest(
    @field:NotNull
    @Schema(description = "권한을 변경할 사용자 ID", example = "1")
    val memberId: Long?,

    @field:NotNull
    @Schema(description = "변경할 권한", example = "STAFF")
    val role: Role?
)