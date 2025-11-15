package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.Part

data class MemberProfileUpdateRequest(
    val name: String,
    val school: String? = null,
    val major: String? = null,
    val part: Part,
    val phoneNumber: String? = null,
)
