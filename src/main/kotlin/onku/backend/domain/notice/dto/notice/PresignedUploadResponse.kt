package onku.backend.domain.notice.dto.notice

data class PresignedUploadResponse(
    val fileId: Long,
    val presignedUrl: String
)