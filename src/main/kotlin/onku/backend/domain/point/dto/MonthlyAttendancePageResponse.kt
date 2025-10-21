package onku.backend.domain.point.dto

import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.global.page.PageResponse
import java.time.LocalDate

data class AttendanceRecordDto(
    val date: LocalDate,
    val attendanceId: Long?,
    val status: AttendancePointType?,
    val point: Int?
)

data class MemberMonthlyAttendanceDto(
    val memberId: Long,
    val name: String,
    val records: List<AttendanceRecordDto>
)

data class MonthlyAttendancePageResponse(
    val year: Int,
    val month: Int,
    val sessionDates: List<Int>,
    val members: PageResponse<MemberMonthlyAttendanceDto>
)