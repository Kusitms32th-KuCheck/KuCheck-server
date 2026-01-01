package onku.backend.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.apple")
data class AppleProps(
    val clientId: String,
    val teamId: String,
    val keyId: String,
    val privateKey: String,
    val redirectUri: String
)