package onku.backend.global.auth.dto

data class AuthHeaders(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val onboardingToken: String? = null
)