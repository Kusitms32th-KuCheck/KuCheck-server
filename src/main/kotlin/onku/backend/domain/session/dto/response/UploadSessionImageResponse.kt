package onku.backend.domain.session.dto.response

data class UploadSessionImageResponse (
    val sessionImageId : Long,
    val sessionImagePreSignedUrl : String
)