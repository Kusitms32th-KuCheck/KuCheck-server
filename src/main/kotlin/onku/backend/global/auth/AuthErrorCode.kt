package onku.backend.global.auth

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class AuthErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {

    OAUTH_EMAIL_SCOPE_REQUIRED("AUTH400", "카카오 프로필에 이메일이 없습니다. 카카오 동의 항목(이메일)을 활성화해 주세요.", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH401", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    EXPIRED_REFRESH_TOKEN("AUTH401", "만료된 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),
    KAKAO_TOKEN_EMPTY_RESPONSE("AUTH502", "카카오 토큰 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_PROFILE_EMPTY_RESPONSE("AUTH502", "카카오 프로필 응답이 비어 있습니다.", HttpStatus.BAD_GATEWAY),
    KAKAO_API_COMMUNICATION_ERROR("AUTH502", "카카오 API 통신 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
}
