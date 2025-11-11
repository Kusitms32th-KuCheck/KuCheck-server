package onku.backend.domain.attendance

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class AttendanceErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    ATTENDANCE_ALREADY_RECORDED("ATT001", "이미 해당 세션에 출석이 완료된 사용자입니다.", HttpStatus.CONFLICT),
    TOKEN_INVALID("ATT002", "유효하지 않거나 이미 사용된 토큰입니다.", HttpStatus.UNAUTHORIZED),
    SESSION_NOT_OPEN("ATT003", "현재 스캔 가능한 세션이 없습니다.", HttpStatus.BAD_REQUEST),
    ATTENDANCE_NOT_FOUND("ATT004", "해당 출석 내역을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_FOR_ATTENDANCE("ATT005", "해당 출석 내역의 사용자와 요청된 memberId가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
}