package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import onku.backend.domain.member.enums.Role

data class UpdateRoleRequest(

    @field:NotNull
    @Schema(description = "변경할 권한", example = "STAFF")
    val role: Role?
)