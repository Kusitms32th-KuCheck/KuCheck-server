package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import onku.backend.domain.member.enums.Part

data class OnboardingRequest (
    @field:NotBlank val name: String,
    @field:NotBlank val school: String,
    @field:NotBlank val major: String,
    val part: Part,
    val phoneNumber: String? = null,

    @Schema(description = "FCM 토큰", example = "eYJhbGciOi...")
    val fcmToken: String? = null,

    @Schema(description = "프로필 이미지 URL", example = "https://s3.../member_profile/1/uuid/profile.png")
    val profileImage: String? = null
)
