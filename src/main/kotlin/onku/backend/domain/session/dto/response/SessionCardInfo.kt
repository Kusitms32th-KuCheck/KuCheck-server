package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.session.enums.SessionCategory
import java.time.LocalDate

data class SessionCardInfo(
    @Schema(description = "세션 ID", example = "1")
    val sessionId : Long,
    @Schema(description = "세션 카테고리", example = "밋업 프로젝트")
    val sessionCategory: SessionCategory,
    @Schema(description = "세션 제목", example = "아이디어 발제 및 커피챗")
    val title : String?,
    @Schema(description = "세션 시작 일자", example = "2025-09-27")
    val startDate : LocalDate?,
    @Schema(description = "공휴일 세션 여부", example = "true")
    val isHoliday : Boolean?,
)
