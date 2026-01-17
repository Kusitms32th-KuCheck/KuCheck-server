package onku.backend.global.context

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class SpringContext : ApplicationContextAware {

    override fun setApplicationContext(ctx: ApplicationContext) {
        context = ctx
    }

    companion object {
        private lateinit var context: ApplicationContext

        fun <T> getBean(type: Class<T>): T =
            context.getBean(type)
    }
}