package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.enums.SocialType

data class MemberInfoListResponse(
    val pendingCount: Long,
    val approvedCount: Long,
    val rejectedCount: Long,
    val members: List<MemberItemResponse>,
)

data class MemberItemResponse(
    val memberId: Long,
    val name: String?,
    val profileImageUrl: String?,
    val part: Part,
    val school: String?,
    val major: String?,
    val phoneNumber: String?,
    val socialType: SocialType,
    val email: String?,
    val approval: ApprovalStatus,
)

data class MemberApprovalListResponse(
    val pendingCount: Long,
    val approvedCount: Long,
    val rejectedCount: Long,
    val members: List<MemberItemResponse>,
)
