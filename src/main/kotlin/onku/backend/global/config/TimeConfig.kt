package onku.backend.global.config


import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class TimeConfig {
     @Bean
     fun systemClock(): Clock = Clock.systemDefaultZone()
}