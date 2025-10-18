package onku.backend.domain.absence.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import onku.backend.domain.absence.dto.annotation.ValidAbsenceReport
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.enums.AbsenceType

class AbsenceReportValidator : ConstraintValidator<ValidAbsenceReport, SubmitAbsenceReportRequest> {
    override fun isValid(
        value: SubmitAbsenceReportRequest?,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value == null) return true

        val type = value.absenceType
        val late = value.lateDateTime
        val leave = value.leaveDateTime

        // 공통적으로 여러 에러메시지를 누적 가능하게 설정
        context.disableDefaultConstraintViolation()

        return when (type) {
            AbsenceType.LATE -> {
                if (leave != null) {
                    context.buildConstraintViolationWithTemplate("지각일 경우 leaveDateTime은 비워야 합니다.")
                        .addPropertyNode("leaveDateTime").addConstraintViolation()
                    false
                } else true
            }
            AbsenceType.EARLY_LEAVE -> {
                if (late != null) {
                    context.buildConstraintViolationWithTemplate("조퇴일 경우 lateDateTime은 비워야 합니다.")
                        .addPropertyNode("lateDateTime").addConstraintViolation()
                    false
                } else true
            }
            AbsenceType.ABSENT -> {
                var valid = true
                if (late != null) {
                    context.buildConstraintViolationWithTemplate("결석일 경우 lateDateTime은 비워야 합니다.")
                        .addPropertyNode("lateDateTime").addConstraintViolation()
                    valid = false
                }
                if (leave != null) {
                    context.buildConstraintViolationWithTemplate("결석일 경우 leaveDateTime은 비워야 합니다.")
                        .addPropertyNode("leaveDateTime").addConstraintViolation()
                    valid = false
                }
                valid
            }
            else -> true
        }
    }
}