package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.Part

data class MemberProfileResponse(
    val name: String?,
    val part: Part
)
