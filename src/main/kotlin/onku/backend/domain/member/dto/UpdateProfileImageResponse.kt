package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema

data class UpdateProfileImageResponse(
    @Schema(description = "수정된 프로필 이미지 URL")
    val profileImageUrl: String
)
