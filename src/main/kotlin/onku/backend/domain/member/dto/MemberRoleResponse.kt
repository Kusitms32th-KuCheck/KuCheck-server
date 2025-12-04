package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.member.enums.Role

data class MemberRoleResponse(
    @Schema(description = "사용자 ID", example = "1")
    val memberId: Long,
    @Schema(description = "변경된 권한", example = "STAFF")
    val role: Role
)