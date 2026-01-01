package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role

data class MemberApprovalResponse(
    val memberId: Long,
    val role: Role,
    val approval: ApprovalStatus
)