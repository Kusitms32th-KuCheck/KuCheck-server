package onku.backend.domain.notice

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class NoticeErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {

    // Category
    CATEGORY_NOT_FOUND("NOT001", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_DUPLICATE("NOT002", "이미 존재하는 카테고리 이름입니다.", HttpStatus.CONFLICT),
    CATEGORY_COLOR_DUPLICATE("NOT003", "이미 사용 중인 카테고리 색상입니다.", HttpStatus.CONFLICT),
    CATEGORY_NAME_TOO_LONG("NOT004", "카테고리 이름은 7자 이상 등록할 수 없습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_LINKED_NOT_DELETABLE("NOT005", "해당 카테고리로 작성된 공지가 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT),
}