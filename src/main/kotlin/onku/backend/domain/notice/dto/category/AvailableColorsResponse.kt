package onku.backend.domain.notice.dto.category

import onku.backend.domain.notice.enums.NoticeCategoryColor

data class AvailableColorsResponse(
    val colors: List<NoticeCategoryColor>
)