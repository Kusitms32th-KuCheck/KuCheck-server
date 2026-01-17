package onku.backend.domain.member.dto

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role

data class MemberResponse(
    val memberId: Long,
    val email: String,
    val role: Role,
    val approval: ApprovalStatus,
    val hasInfo: Boolean
) {
    companion object {
        fun of(member: Member): MemberResponse =
            MemberResponse(
                memberId = member.id!!,
                email = member.email!!,
                role = member.role,
                approval = member.approval,
                hasInfo = member.hasInfo
            )
    }
}
