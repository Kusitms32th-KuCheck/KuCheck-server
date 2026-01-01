package onku.backend.global.auth

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {

    OAUTH_EMAIL_SCOPE_REQUIRED("AUTH400", "응답값에 이메일이 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH401", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("AUTH401", "만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_TOKEN_EMPTY_RESPONSE("AUTH502", "카카오 토큰 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_PROFILE_EMPTY_RESPONSE("AUTH502", "카카오 프로필 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_API_COMMUNICATION_ERROR("AUTH502", "카카오 API 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    INVALID_REDIRECT_URI("AUTH400", "유효하지 않은 리다이렉트 URI입니다.", HttpStatus.BAD_REQUEST),
    APPLE_API_COMMUNICATION_ERROR("AUTH502", "애플 API 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    APPLE_TOKEN_EMPTY_RESPONSE("AUTH502", "애플 토큰 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
    APPLE_ID_TOKEN_INVALID("AUTH401", "유효하지 않은 애플 ID 토큰입니다.", HttpStatus.UNAUTHORIZED),
    APPLE_JWKS_FETCH_FAILED("AUTH502", "애플 공개 키(JWKS)를 불러오는 데 실패했습니다.", HttpStatus.BAD_GATEWAY)
}
