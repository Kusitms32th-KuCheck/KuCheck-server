package onku.backend.domain.notice.dto.notice

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.notice.enums.NoticeCategoryColor

@Schema(description = "공지에 표시되는 카테고리 정보")
data class CategoryBadge(

    @Schema(description = "카테고리 이름", example = "공지")
    val name: String,

    @Schema(description = "카테고리 색상", example = "YELLOW")
    val color: NoticeCategoryColor
)