package onku.backend.domain.notice.dto.notice

import onku.backend.domain.notice.enums.NoticeStatus

data class NoticeListItemResponse(
    val id: Long,
    val title: String?,
    val content: String?,
    val authorId: Long,
    val authorName: String?,
    val categories: List<CategoryBadge>,
    val createdAt: String,
    val status: NoticeStatus?,
    val imageUrls: List<NoticeFileWithUrl>,
    val fileUrls: List<NoticeFileWithUrl>
)