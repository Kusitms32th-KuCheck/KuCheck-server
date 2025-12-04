package onku.backend.domain.point.dto

import onku.backend.domain.member.enums.Part

data class AdminPointOverviewDto(
    val memberId: Long,
    val name: String?,
    val part: Part,
    val phoneNumber: String?,
    val school: String?,
    val major: String?,
    val isTf: Boolean,
    val isStaff: Boolean,

    val attendanceMonthlyTotals: Map<Int, Int>, // 월별 출석 점수
    val kupickParticipation: Map<Int, Boolean>, // 월별 큐픽 참여 여부

    val studyPoints: Int,
    val kuportersPoints: Int,
    val memo: String?
)
