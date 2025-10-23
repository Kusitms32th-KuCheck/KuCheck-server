package onku.backend.domain.session.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "세션 종류",
    example = "CORPORATE_PROJECT"
)
enum class SessionCategory {
    @Schema(description = "기업 프로젝트") CORPORATE_PROJECT,
    @Schema(description = "밋업 프로젝트") MEETUP_PROJECT,
    @Schema(description = "네트워킹") NETWORKING,
    @Schema(description = "공휴일 특별 세션 (가산점 1점)") HOLIDAY,
    @Schema(description = "휴회 세션") REST,
}