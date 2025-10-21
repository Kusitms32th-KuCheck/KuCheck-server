package onku.backend.domain.point.dto

data class MemberMonthlyAttendanceDto(
    val memberId: Long,
    val name: String,
    val records: List<AttendanceRecordDto>
)