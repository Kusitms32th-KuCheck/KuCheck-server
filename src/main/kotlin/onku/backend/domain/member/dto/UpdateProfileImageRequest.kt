package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateProfileImageRequest(
    @field:NotBlank
    @field:Size(max = 2048)
    @Schema(description = "새 프로필 이미지 URL", example = "https://s3.../member_profile/1/uuid/profile.png")
    val imageUrl: String
)
