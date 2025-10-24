package onku.backend.domain.attendance.dto

import java.time.LocalDateTime

data class AttendanceTokenCore(
    val token: String,
    val expAt: LocalDateTime
)
