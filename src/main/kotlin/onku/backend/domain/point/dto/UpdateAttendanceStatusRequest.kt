package onku.backend.domain.point.dto

import jakarta.validation.constraints.NotNull
import onku.backend.domain.attendance.enums.AttendancePointType

data class UpdateAttendanceStatusRequest(
    @field:NotNull
    val attendanceId: Long,
    @field:NotNull
    val memberId: Long,
    @field:NotNull
    val status: AttendancePointType
)