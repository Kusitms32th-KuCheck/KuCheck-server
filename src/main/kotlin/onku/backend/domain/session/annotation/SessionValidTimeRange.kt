package onku.backend.domain.session.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import onku.backend.domain.session.validator.SessionTimeValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [SessionTimeValidator::class])
annotation class SessionValidTimeRange(
    val message: String = "Start time cannot be after end time.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
