package onku.backend.global.auth.dto

import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role

data class AuthLoginResult(
    val status: ApprovalStatus,
    val memberId: Long? = null,
    val role: Role? = null,
)