package onku.backend.domain.member

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class MemberErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    MEMBER_NOT_FOUND("MEMBER404", "회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_MEMBER_STATE("MEMBER409", "현재 상태에서는 요청한 상태 변경을 수행할 수 없습니다.", HttpStatus.CONFLICT),
    PAGE_MEMBERS_NOT_FOUND("MEMBER404_PAGE", "조회할 수 있는 회원이 없습니다.", HttpStatus.NOT_FOUND),
}
