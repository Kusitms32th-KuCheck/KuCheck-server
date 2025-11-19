package onku.backend.domain.notice.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지 카테고리 색상")
enum class NoticeCategoryColor {
    @Schema(description = "빨강")
    RED,

    @Schema(description = "주황")
    ORANGE,

    @Schema(description = "노랑")
    YELLOW,

    @Schema(description = "초록")
    GREEN,

    @Schema(description = "연두")
    LIGHT_GREEN,

    @Schema(description = "청록")
    TEAL,

    @Schema(description = "파랑")
    BLUE,

    @Schema(description = "보라")
    PURPLE,

    @Schema(description = "분홍")
    PINK,

    @Schema(description = "갈색")
    BROWN;
}