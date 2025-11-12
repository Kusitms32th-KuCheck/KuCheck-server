package onku.backend.domain.notice.dto.category

import onku.backend.domain.notice.NoticeCategory
import onku.backend.domain.notice.enums.NoticeCategoryColor

data class NoticeCategoryResponse(
    val id: Long,
    val name: String,
    val color: NoticeCategoryColor
) {
    companion object {
        fun from(entity: NoticeCategory) = NoticeCategoryResponse(
            id = entity.id!!,
            name = entity.name,
            color = entity.color
        )
    }
}