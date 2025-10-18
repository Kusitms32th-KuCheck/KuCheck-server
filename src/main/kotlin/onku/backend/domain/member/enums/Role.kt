package onku.backend.domain.member.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "사용자 권한 종류 (계층: MANAGEMENT ⟶ ADMIN ⟶ USER ⟶ GUEST)",
    example = "USER"
)
enum class Role {
    @Schema(description = "게스트(온보딩 전용)") GUEST,
    @Schema(description = "일반 사용자") USER,
    @Schema(description = "운영진(사용자 권한 포함)") ADMIN,
    @Schema(description = "경영/관리(운영진+사용자 권한 포함)") MANAGEMENT;

    fun authorities(): List<String> = when (this) {
        GUEST -> listOf("GUEST")
        USER -> listOf("USER")
        ADMIN -> listOf("ADMIN", "USER")
        MANAGEMENT -> listOf("MANAGEMENT", "ADMIN", "USER")
    }
}
