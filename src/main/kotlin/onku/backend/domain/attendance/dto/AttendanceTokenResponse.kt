package onku.backend.domain.attendance.dto

import onku.backend.domain.member.enums.Part
import java.time.LocalDateTime

data class AttendanceTokenResponse(
    val token: String,
    val expAt: LocalDateTime,
    val name: String,
    val part: Part,
    val school: String?,
    val profileImageUrl: String?
)