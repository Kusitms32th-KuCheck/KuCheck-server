package onku.backend.domain.member.dto

import jakarta.validation.constraints.NotBlank
import onku.backend.domain.member.enums.Part

data class OnboardingRequest (
    @field:NotBlank val name: String,
    val school: String? = null,
    val major: String? = null,
    val part: Part,
    val phoneNumber: String? = null
)
