package onku.backend.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    SERVER_UNTRACKED_ERROR("COMMON500", "미등록 서버 에러입니다. 서버 팀에 연락주세요.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST("COMMON400", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("COMMON401", "인증되지 않은 요청입니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("COMMON403", "권한이 부족합니다.", HttpStatus.FORBIDDEN),
    INVALID_PARAMETER("COMMON422", "잘못된 파라미터입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    PARAMETER_VALIDATION_ERROR("COMMON422", "파라미터 검증 에러입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    PARAMETER_GRAMMAR_ERROR("COMMON422", "파라미터 문법 에러입니다.", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_FILE_EXTENSION("S3001", "올바르지 않은 파일 확장자 입니다.", HttpStatus.BAD_REQUEST),
    FCM_ACCESS_TOKEN_FAIL("alarm001", "FCM 액세스 토큰 발급 중에 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    SESSION_NOT_FOUND("session001", "해당 세션이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    SESSION_PAST("session002", "이미 지난 세션입니다.", HttpStatus.BAD_REQUEST),
    SESSION_IMMINENT("session003", "불참사유서 제출은 목요일까지입니다.", HttpStatus.BAD_REQUEST),
    INVALID_SESSION("session004", "유효한 세션이 아닙니다.", HttpStatus.BAD_REQUEST),
    SQL_INTEGRITY_VIOLATION("sql001", "무결성 제약조건을 위반하였습니다.", HttpStatus.BAD_REQUEST);
}
