package onku.backend.domain.session

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class SessionErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    SESSION_DETAIL_NOT_FOUND("SESSION_DETAIL404", "세션 상세정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
}