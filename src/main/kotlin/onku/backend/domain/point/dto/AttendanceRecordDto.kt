package onku.backend.domain.point.dto

import onku.backend.domain.attendance.enums.AttendanceStatus
import java.time.LocalDate

data class AttendanceRecordDto(
    val date: LocalDate,
    val attendanceId: Long?,
    val status: AttendanceStatus?,
    val point: Int?
)