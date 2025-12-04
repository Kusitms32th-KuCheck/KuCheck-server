package onku.backend.domain.session.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SessionImageDto (
    @Schema(description = "세션 이미지 ID", example = "1")
    val sessionImageId : Long,
    @Schema(description = "세션 이미지 PreSignedUrl", example = "https://~~")
    val sessionImagePreSignedUrl : String,
    @Schema(description = "세션 원본 이미지 이름", example = "example.png")
    val sessionOriginalFileName : String
)