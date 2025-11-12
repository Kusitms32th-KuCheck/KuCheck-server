package onku.backend.domain.point.dto

import onku.backend.domain.attendance.enums.AttendancePointType
import java.time.LocalDateTime

data class UpdateAttendanceStatusResponse(
    val attendanceId: Long,
    val memberId: Long,
    val oldStatus: AttendancePointType,
    val newStatus: AttendancePointType,
    val diff: Int,
    val week: Long?,
    val occurredAt: LocalDateTime
)