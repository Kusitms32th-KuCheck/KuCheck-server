package onku.backend.domain.absence

import onku.backend.global.exception.ApiErrorCode
import org.springframework.http.HttpStatus

enum class AbsenceReportErrorCode(
    override val errorCode: String,
    override val message: String,
    override val status: HttpStatus
) : ApiErrorCode {
    ABSENCE_REPORT_NOT_FOUND("ABSENCE404", "해당하는 불참사유서를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
}