package onku.backend.domain.member.dto

import jakarta.validation.constraints.NotBlank
import onku.backend.domain.member.enums.Part

data class OnboardingRequest (
    @field:NotBlank val name: String,
    @field:NotBlank val school: String,
    @field:NotBlank val major: String,
    val part: Part,
    val phoneNumber: String? = null
)
