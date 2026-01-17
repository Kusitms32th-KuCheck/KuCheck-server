package onku.backend.domain.notice.dto.notice

data class NoticeDetailResponse(
    val id: Long,
    val title: String?,
    val categories: List<CategoryBadge>,
    val createdAt: String,
    val content: String?,
    val authorId: Long,
    val authorName: String?,
    val imageUrls: List<NoticeFileWithUrl>,
    val fileUrls: List<NoticeFileWithUrl>
)