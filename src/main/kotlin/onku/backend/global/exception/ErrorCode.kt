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
    KUPICK_APPLICATION_FIRST("kupick001", "큐픽 신청부터 진행해주세요", HttpStatus.BAD_REQUEST),
    KUPICK_NOT_FOUND("kupick002", "해당 큐픽 객체를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FCM_ACCESS_TOKEN_FAIL("alarm001", "FCM 액세스 토큰 발급 중에 오류가 발생했습니다.", HttpStatus.BAD_REQUEST);
}
