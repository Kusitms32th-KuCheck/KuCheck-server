package onku.backend.global.config

import onku.backend.global.auth.enums.KakaoEnv
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth.kakao")
data class KakaoProps(
    val clientId: String,
    val adminKey: String,
    val redirectMap: Map<KakaoEnv, String> = emptyMap()
)