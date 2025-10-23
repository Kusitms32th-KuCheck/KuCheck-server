package onku.backend.domain.session.dto

import io.swagger.v3.oas.annotations.media.Schema

data class SessionImageDto (
    @Schema(description = "세션 이미지 ID", example = "1")
    val sessionImageId : Long,
    @Schema(description = "세션 이미지 PreSignedUrl", example = "https://~~")
    val sessionImagePreSignedUrl : String
)