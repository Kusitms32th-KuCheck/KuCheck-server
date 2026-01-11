package onku.backend.domain.notice.util

import onku.backend.domain.notice.Notice
import onku.backend.domain.notice.NoticeCategory
import onku.backend.domain.notice.dto.notice.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val fmtList = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
private val fmtDetail = DateTimeFormatter.ofPattern("MM/dd HH:mm")

object NoticeDtoMapper {
    fun toCategoryBadge(c: NoticeCategory) =
        CategoryBadge(name = c.name, color = c.color)

    fun toListItem(
        n: Notice,
        imageFiles: List<NoticeFileWithUrl>,
        fileFiles: List<NoticeFileWithUrl>
    ) = NoticeListItemResponse(
        id = n.id!!,
        title = n.title,
        content = n.content,
        authorId = n.member.id!!,
        authorName = n.member.memberProfile?.name,
        categories = n.categories.map(::toCategoryBadge),
        createdAt = (n.publishedAt ?: LocalDateTime.now()).format(fmtList),
        status = n.status,
        imageUrls = imageFiles,
        fileUrls = fileFiles
    )

    fun toDetail(
        n: Notice,
        imageFiles: List<NoticeFileWithUrl>,
        fileFiles: List<NoticeFileWithUrl>
    ) = NoticeDetailResponse(
        id = n.id!!,
        title = n.title,
        categories = n.categories.map(::toCategoryBadge),
        createdAt = (n.publishedAt ?: LocalDateTime.now()).format(fmtDetail),
        content = n.content,
        authorId = n.member.id!!,
        authorName = n.member.memberProfile?.name,
        imageUrls = imageFiles,
        fileUrls = fileFiles
    )
}