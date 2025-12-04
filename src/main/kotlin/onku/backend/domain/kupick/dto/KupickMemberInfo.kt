package onku.backend.domain.kupick.dto

import onku.backend.domain.member.Member
import java.time.LocalDateTime

data class KupickMemberInfo(
    val member: Member,
    val submitDate: LocalDateTime?
)
