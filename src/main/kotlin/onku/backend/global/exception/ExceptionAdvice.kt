package onku.backend.global.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import onku.backend.global.response.ErrorResponse
import onku.backend.global.response.result.ExceptionResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionAdvice {

    /**
     * 등록되지 않은 에러
     */
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleUntrackedException(
        e: Exception,
        req: HttpServletRequest
    ): ErrorResponse<ExceptionResult.ServerErrorData> {
        val serverErrorData = ExceptionResult.ServerErrorData(
            errorClass = e.javaClass.name,
            errorMessage = e.message
        )

        return ErrorResponse.ok(
            ErrorCode.SERVER_UNTRACKED_ERROR.errorCode,
            ErrorCode.SERVER_UNTRACKED_ERROR.message,
            serverErrorData
        )
    }

    /**
     * 파라미터 검증 예외 (@Valid, @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    fun handleValidationExceptions(
        e: MethodArgumentNotValidException
    ): ErrorResponse<List<ExceptionResult.ParameterData>> {
        val list = e.bindingResult.fieldErrors.map { fieldError: FieldError ->
            ExceptionResult.ParameterData(
                key = fieldError.field,
                value = fieldError.rejectedValue?.toString(),
                reason = fieldError.defaultMessage
            )
        }

        return ErrorResponse.ok(
            ErrorCode.PARAMETER_VALIDATION_ERROR.errorCode,
            ErrorCode.PARAMETER_VALIDATION_ERROR.message,
            list
        )
    }

    /**
     * 파라미터 문법 예외 (JSON 파싱 등)
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    fun handleHttpMessageParsingExceptions(
        e: HttpMessageNotReadableException
    ): ErrorResponse<String?> {
        return ErrorResponse.ok(
            ErrorCode.PARAMETER_GRAMMAR_ERROR.errorCode,
            ErrorCode.PARAMETER_GRAMMAR_ERROR.message,
            e.message
        )
    }

    /**
     * 요청 파라미터(@RequestParam 등) 유효성 검증 실패 예외
     */
    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    fun handleConstraintViolationException(
        e: ConstraintViolationException
    ): ErrorResponse<List<ExceptionResult.ParameterData>> {
        val list = e.constraintViolations.map { violation ->
            val fieldPath = violation.propertyPath.toString() // 예: "checkEmailDuplicate.email"
            val field = if ('.' in fieldPath) {
                fieldPath.substring(fieldPath.lastIndexOf('.') + 1)
            } else {
                fieldPath
            }

            val value = violation.invalidValue?.toString() ?: "null"
            val reason = violation.message

            ExceptionResult.ParameterData(
                key = field,
                value = value,
                reason = reason
            )
        }

        return ErrorResponse.ok(
            ErrorCode.PARAMETER_VALIDATION_ERROR.errorCode,
            ErrorCode.PARAMETER_VALIDATION_ERROR.message,
            list
        )
    }

    /**
     * 커스텀 예외
     */
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(
        e: CustomException
    ): ResponseEntity<ErrorResponse<*>> {
        val code = e.errorCode
        val body = ErrorResponse.of<Any>(code.errorCode, code.message)
        return ResponseEntity(body, code.status)
    }
}
