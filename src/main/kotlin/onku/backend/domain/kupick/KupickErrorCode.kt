package onku.backend.domain.kupick

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class KupickErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    KUPICK_SAVE_FAILED("KUPICK500_SAVE", "큐픽 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    KUPICK_APPLICATION_FIRST("kupick001", "큐픽 신청부터 진행해주세요", HttpStatus.BAD_REQUEST),
    KUPICK_NOT_FOUND("kupick002", "해당 큐픽 객체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    KUPICK_NOT_UPDATE("kupick003", "이미 승인된 큐픽은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST)
}