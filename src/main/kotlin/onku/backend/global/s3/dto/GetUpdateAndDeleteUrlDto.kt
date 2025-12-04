package onku.backend.global.s3.dto

import io.swagger.v3.oas.annotations.media.Schema

data class GetUpdateAndDeleteUrlDto(
    @Schema(description = "업로드 할 파일의 url(업로드용 presignedUrl)", example = "https://S3:~")
    val newUrl : String?,
    @Schema(description = "오래된 파일의 url(삭제용 presignedUrl)", example = "https://S3:~")
    val oldUrl : String?,
)
