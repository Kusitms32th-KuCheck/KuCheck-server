package onku.backend.domain.member.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "학회원 정보 중 파트 구분",
    example = "BACKEND"
)
enum class Part {
    @Schema(description = "백엔드") BACKEND,
    @Schema(description = "프론트엔드") FRONTEND,
    @Schema(description = "디자인") DESIGN,
    @Schema(description = "기획") PLANNING
}