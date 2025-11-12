package onku.backend.domain.absence.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.absence.enums.AbsenceApprovedType
import onku.backend.domain.absence.enums.AbsenceSubmitType
import onku.backend.domain.member.enums.Part
import java.time.LocalDate
import java.time.LocalTime

data class GetMemberAbsenceReportResponse(
    @Schema(description = "학회원 이름", example = "홍길동")
    val name : String,
    @Schema(description = "파트", example = "BACKEND")
    val part : Part,
    @Schema(description = "불참사유서 id", example = "1")
    val absenceReportId : Long,
    @Schema(description = "제출일시", example = "2025-11-08")
    val submitDate : LocalDate,
    @Schema(description = "불참여부", example = "ABSENT / LATE / EARLY_LEAVE")
    val submitType : AbsenceSubmitType,
    @Schema(description = "시간", example = "13:00")
    val time : LocalTime?,
    @Schema(description = "사유", example = "코딩테스트 면접")
    val reason : String,
    @Schema(description = "증빙서류 presignedUrl", example = "https://s3~")
    val url : String?,
    @Schema(description = "벌점", example = "EXCUSED / ABSENT / ABSENT_WITH_DOC / ABSENT_WITH_CAUSE / LATE / EARLY_LEAVE / null")
    val absenceApprovedType: AbsenceApprovedType?
)
