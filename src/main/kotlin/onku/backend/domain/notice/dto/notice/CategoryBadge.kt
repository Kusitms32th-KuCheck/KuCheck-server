package onku.backend.domain.notice.dto.notice

import onku.backend.domain.notice.enums.NoticeCategoryColor

data class CategoryBadge(
    val name: String,
    val color: NoticeCategoryColor
)