package onku.backend.domain.notice.dto.notice

data class NoticeListResponse(
    val totalCount: Long,
    val items: List<NoticeListItemResponse>
)