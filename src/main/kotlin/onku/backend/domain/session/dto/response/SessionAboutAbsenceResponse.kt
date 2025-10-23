package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema

data class SessionAboutAbsenceResponse(
    @Schema(description = "세션 ID", example = "1")
    val sessionId : Long?,
    @Schema(description = "세션 제목", example = "전문가 초청 강연")
    val title : String,
    @Schema(description = "세션 주차", example = "1")
    val week : Long,
    @Schema(description = "활성화 여부", example = "true")
    val active : Boolean
)
