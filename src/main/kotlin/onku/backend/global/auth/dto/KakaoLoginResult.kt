package onku.backend.global.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LoginResult(
    val status: ApprovalStatus,
    val memberId: Long? = null,
    val role: Role? = null,
    val allowedEndpoint: String? = null,
    val expiresInMinutes: Long? = null
)