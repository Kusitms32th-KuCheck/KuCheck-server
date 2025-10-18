package onku.backend.domain.absence.dto.annotation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import onku.backend.domain.absence.validator.AbsenceReportValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [AbsenceReportValidator::class])
annotation class ValidAbsenceReport(
    val message: String = "Invalid absence report combination",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
