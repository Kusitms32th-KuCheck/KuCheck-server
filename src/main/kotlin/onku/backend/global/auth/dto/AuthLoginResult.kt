package onku.backend.global.auth.dto

import onku.backend.domain.member.enums.ApprovalStatus

data class AuthLoginResult(
    val status: ApprovalStatus,
    val memberId: Long? = null,
    val role: String? = null,
)