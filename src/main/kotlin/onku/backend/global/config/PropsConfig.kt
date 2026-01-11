package onku.backend.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    value = [
        KakaoProps::class,
        AppleProps::class
    ]
)
class PropsConfig