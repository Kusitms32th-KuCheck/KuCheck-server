package onku.backend.domain.attendance.dto

import onku.backend.domain.attendance.enums.AttendanceStatus
import java.time.LocalDateTime

data class AttendanceResponse(
    val memberId: Long,
    val memberName: String,
    val sessionId: Long,
    val state: AttendanceStatus,
    val scannedAt: LocalDateTime
)