package onku.backend.global.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoAccount(
    @JsonProperty("email") val email: String?,
    @JsonProperty("has_email") val hasEmail: Boolean? = null,
    @JsonProperty("email_needs_agreement") val emailNeedsAgreement: Boolean? = null,
    @JsonProperty("is_email_valid") val isEmailValid: Boolean? = null,
    @JsonProperty("is_email_verified") val isEmailVerified: Boolean? = null
)