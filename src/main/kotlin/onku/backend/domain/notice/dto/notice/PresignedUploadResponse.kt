package onku.backend.domain.notice.dto.notice

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공지 파일 업로드를 위한 프리사인드 URL 응답")
data class PresignedUploadResponse(

    @Schema(description = "업로드할 파일의 ID", example = "3")
    val fileId: Long,

    @Schema(
        description = "S3 업로드용 프리사인드 URL (PUT 요청에 사용)",
        example = "https://..."
    )
    val presignedUrl: String
)