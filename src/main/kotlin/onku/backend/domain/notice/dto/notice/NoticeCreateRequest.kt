package onku.backend.domain.notice.dto.notice

data class NoticeCreateRequest(
    val title: String,
    val categoryIds: List<Long>,
    val content: String,
    val fileIds: List<Long> = emptyList()
)
typealias NoticeUpdateRequest = NoticeCreateRequest