package onku.backend.domain.session.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "세션 종류",
    example = "GENERAL"
)
enum class SessionCategory {
    @Schema(description = "일반 세션") GENERAL,
    @Schema(description = "공휴일 특별 세션 (가산점 1점)") HOLIDAY,
    @Schema(description = "휴회 세션") REST,
}