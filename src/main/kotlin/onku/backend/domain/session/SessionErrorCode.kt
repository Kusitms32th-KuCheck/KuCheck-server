package onku.backend.domain.session

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class SessionErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    SESSION_DETAIL_NOT_FOUND("SESSION_DETAIL404", "세션 상세정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_IMAGE_NOT_FOUND("SESSION_IMAGE404", "세션 이미지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SESSION_NOT_FOUND("session001", "해당 세션이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    SESSION_PAST("session002", "이미 지난 세션입니다.", HttpStatus.BAD_REQUEST),
    SESSION_IMMINENT("session003", "불참사유서 제출은 목요일까지입니다.", HttpStatus.BAD_REQUEST),
    INVALID_SESSION("session004", "유효한 세션이 아닙니다.", HttpStatus.BAD_REQUEST),
}