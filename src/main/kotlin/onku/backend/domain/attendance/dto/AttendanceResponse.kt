package onku.backend.domain.attendance.dto

import onku.backend.domain.attendance.enums.AttendancePointType
import java.time.LocalDateTime

data class AttendanceResponse(
    val memberId: Long,
    val memberName: String,
    val sessionId: Long,
    val state: AttendancePointType,
    val scannedAt: LocalDateTime,
    val thisWeekSummary: WeeklyAttendanceSummary
)