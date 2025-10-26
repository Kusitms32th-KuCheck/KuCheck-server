package onku.backend.domain.member.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "사용자 권한 계층: EXECUTIVE(회장단) > MANAGEMENT(경총) > STAFF(운영진) > USER(학회원) > GUEST(온보딩)",
    example = "USER"
)
enum class Role {
    @Schema(description = "게스트 (온보딩 전용)") GUEST,
    @Schema(description = "학회원 (일반 사용자)") USER,
    @Schema(description = "운영진 (사용자 권한 포함)") STAFF,
    @Schema(description = "경총 (운영진+사용자 권한 포함)") MANAGEMENT,
    @Schema(description = "회장단 (경총+운영진+사용자 권한 포함)") EXECUTIVE;

    fun authorities(): List<String> = when (this) {
        GUEST -> listOf("GUEST")
        USER -> listOf("USER")
        STAFF -> listOf("STAFF", "USER")
        MANAGEMENT -> listOf("MANAGEMENT", "STAFF", "USER")
        EXECUTIVE -> listOf("EXECUTIVE", "MANAGEMENT", "STAFF", "USER")
    }
}
