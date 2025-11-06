package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.session.enums.SessionCategory
import java.time.LocalDate

data class GetInitialSessionResponse(
    @Schema(description = "세션 ID", example = "1")
    val sessionId : Long,
    @Schema(description = "세션 시작 일자", example = "2025-10-21")
    val startDate : LocalDate,
    @Schema(description = "세션 제목", example = "전문가 초청 강의")
    val title : String,
    @Schema(description = "세션 종류", example = "밋업프로젝트")
    val category : SessionCategory,
    @Schema(description = "세션 상세정보", example = "만약 null이면 세션정보 입력여부 false로 하면 됨")
    val sessionDetailId : Long?,
    @Schema(description = "공휴일 세션 여부", example = "true")
    val isHoliday : Boolean
)
