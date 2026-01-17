package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.session.dto.SessionImageDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class GetSessionNoticeResponse(
    @Schema(description = "세션 관련 정보", example = "1")
    val sessionId : Long,
    @Schema(description = "세션 상세 ID", example = "1")
    val sessionDetailId : Long,
    @Schema(description = "세션 제목", example = "아이디어 발표 & 커피챗 세션")
    val title : String?,
    @Schema(description = "세션 장소", example = "마루 180")
    val place : String?,
    @Schema(description = "시작 일자", example = "2025-11-01")
    val startDate : LocalDate?,
    @Schema(description = "시작 시각", example = "13:00")
    val startTime : LocalTime?,
    @Schema(description = "종료 시각", example = "17:00")
    val endTime : LocalTime?,
    @Schema(description = "공지내용 데이터", example = "세션합니당")
    val content : String?,
    @Schema(description = "세션 관련 이미지", example = "객체임")
    val images : List<SessionImageDto>,
    @Schema(description = "공휴일 세션 여부", example = "true")
    val isHoliday : Boolean?,
    @Schema(description = "세션 초기작성 시간(처음 생성)", example = "2025-07-15T14:00:00")
    val createdAt : LocalDateTime,
    @Schema(description = "세션 마지막 업데이트 시간", example = "2025-07-15T14:00:00")
    val updatedAt : LocalDateTime
)
