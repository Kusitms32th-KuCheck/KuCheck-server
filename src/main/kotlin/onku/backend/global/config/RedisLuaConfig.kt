package onku.backend.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.core.script.DefaultRedisScript

@Configuration
class RedisLuaConfig {

    @Bean
    fun attendanceSwapScript(): DefaultRedisScript<String> =
        DefaultRedisScript<String>().apply {
            setLocation(ClassPathResource("lua/issue_token.lua"))
            setResultType(String::class.java)
        }

    @Bean
    fun attendanceConsumeScript(): DefaultRedisScript<String> =
        DefaultRedisScript<String>().apply {
            setLocation(ClassPathResource("lua/consume_token.lua"))
            setResultType(String::class.java)
        }
}
