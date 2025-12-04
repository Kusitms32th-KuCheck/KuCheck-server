package onku.backend.domain.point.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "단일 상/벌점 레코드 응답")
data class MemberPointHistory(
    val date: String,
    val type: String,
    val points: Int,
    val week: Long? = null,
    val attendanceTime: String? = null,
    val earlyLeaveTime: String? = null
)

@Schema(description = "사용자 상/벌점 이력 응답")
data class MemberPointHistoryResponse(
    val memberId: Long,
    val name: String? = null,
    val plusPoints: Int,
    val minusPoints: Int,
    val totalPoints: Int,
    val records: List<MemberPointHistory>,
    val totalPages: Int,
    val isLastPage: Boolean
)