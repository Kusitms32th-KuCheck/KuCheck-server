package onku.backend.domain.member.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "소셜 로그인 종류",
    example = "KAKAO"
)
enum class SocialType {
    @Schema(description = "카카오") KAKAO,
    @Schema(description = "애플") APPLE,
    @Schema(description = "이메일") EMAIL,
}