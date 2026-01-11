package onku.backend.domain.notice.dto.notice

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지 첨부파일 + presigned URL")
data class NoticeFileWithUrl(

    @Schema(description = "첨부파일 ID", example = "1")
    val id: Long,

    @Schema(description = "첨부파일 다운로드 URL")
    val url: String,

    @Schema(description = "첨부파일 크기", example = "123456")
    val size: Long?,

    @Schema(description = "파일원본 이름", example = "string.png")
    val originalFileName : String,
)