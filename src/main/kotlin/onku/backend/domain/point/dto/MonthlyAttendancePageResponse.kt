package onku.backend.domain.point.dto

import onku.backend.global.page.PageResponse

data class MonthlyAttendancePageResponse(
    val year: Int,
    val month: Int,
    val sessionDates: List<Int>,
    val members: PageResponse<MemberMonthlyAttendanceDto>
)
