package onku.backend.domain.kupick

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class KupickErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    KUPICK_SAVE_FAILED("KUPICK500_SAVE", "큐픽 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
}