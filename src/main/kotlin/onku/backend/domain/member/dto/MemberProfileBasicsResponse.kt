package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.Part

data class MemberProfileBasicsResponse(
    val name: String,
    val part: Part,
    val school: String?,
    val profileImageUrl: String?
)
