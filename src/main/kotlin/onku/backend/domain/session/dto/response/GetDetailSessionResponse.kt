package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.session.dto.SessionImageDto
import java.time.LocalTime

data class GetDetailSessionResponse(
    @Schema(description = "세션 상세 정보 ID", example = "1")
    val sessionDetailId : Long,
    @Schema(description = "세션 장소", example = "마루180")
    val place : String?,
    @Schema(description = "세션 시작 시각", example = "09:00:00")
    val startTime : LocalTime?,
    @Schema(description = "세션 종료 시각", example = "15:00:00")
    val endTime : LocalTime?,
    @Schema(description = "본문", example = "어쩌구 저쩌구")
    val content : String?,
    @Schema(description = "세션 이미지", example = "리스트형식")
    val sessionImages : List<SessionImageDto>
)
