package onku.backend.domain.notice.dto.notice

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.notice.enums.NoticeStatus

@Schema(description = "공지 한 개 응답")
data class NoticeListItemResponse(

    @Schema(description = "공지 ID", example = "1")
    val id: Long,

    @Schema(description = "공지 제목", example = "11월 3주차 온큐 공지")
    val title: String?,

    @Schema(description = "공지 내용(일부 또는 전체)", example = "이번 주 스터디 공지입니다.")
    val content: String?,

    @Schema(description = "작성자 회원 ID", example = "10")
    val authorId: Long,

    @Schema(description = "작성자 이름", example = "김온큐")
    val authorName: String?,

    @Schema(description = "공지에 연결된 카테고리 뱃지 리스트")
    val categories: List<CategoryBadge>,

    @Schema(
        description = "공지 작성(또는 게시)일시, 포맷: YYYY/MM/DD HH:mm",
        example = "2025/11/15 13:30"
    )
    val createdAt: String,

    @Schema(
        description = "공지 상태 (예: DRAFT, PUBLISHED 등)",
        example = "PUBLISHED",
        nullable = true
    )
    val status: NoticeStatus?,

    @Schema(description = "공지에 첨부된 이미지 파일들의 URL 리스트")
    val imageUrls: List<NoticeFileWithUrl>,

    @Schema(description = "공지에 첨부된 일반 파일들의 URL 리스트")
    val fileUrls: List<NoticeFileWithUrl>
)