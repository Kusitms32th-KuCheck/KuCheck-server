package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalTime

data class ThisWeekSessionInfo(
    @Schema(description = "세션 ID", example = "1")
    val sessionId : Long,
    @Schema(description = "세션 상세 정보 ID", example = "1")
    val sessionDetailId : Long?,
    @Schema(description = "세션 제목", example = "아이디어 발제 및 커피챗")
    val title : String?,
    @Schema(description = "세션 장소", example = "마루 180")
    val place : String?,
    @Schema(description = "세션 시작 일자", example = "2025-09-22")
    val startDate : LocalDate?,
    @Schema(description = "시작 시각", example = "13:00")
    val startTime : LocalTime?,
    @Schema(description = "종료 시각", example = "16:00")
    val endTime : LocalTime?,
    @Schema(description = "공휴일 세션 여부", example = "true")
    val isHoliday : Boolean?,
)
