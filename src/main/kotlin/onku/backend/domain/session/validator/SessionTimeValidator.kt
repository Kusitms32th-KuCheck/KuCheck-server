package onku.backend.domain.session.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.annotation.SessionValidTimeRange

class SessionTimeValidator : ConstraintValidator<SessionValidTimeRange, UpsertSessionDetailRequest> {
    override fun isValid(value: UpsertSessionDetailRequest?, context: ConstraintValidatorContext): Boolean {
        if (value == null) return true
        context.buildConstraintViolationWithTemplate("시작시간이 끝 시간보다 앞설 수 없습니다.")
            .addPropertyNode("startTime").addConstraintViolation()
        return value.startTime.isBefore(value.endTime)
    }
}