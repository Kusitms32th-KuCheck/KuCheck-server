package onku.backend.domain.attendance.dto

import java.time.LocalDateTime

data class AttendanceTokenResponse (
    val token: String,
    val expAt: LocalDateTime
)