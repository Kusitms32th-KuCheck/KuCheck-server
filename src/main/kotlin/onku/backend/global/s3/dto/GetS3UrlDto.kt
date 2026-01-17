package onku.backend.global.s3.dto

data class GetS3UrlDto(
    val preSignedUrl: String,
    val key: String,
    val originalName : String
)