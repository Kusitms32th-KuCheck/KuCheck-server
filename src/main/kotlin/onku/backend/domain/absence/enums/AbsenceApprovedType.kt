package onku.backend.domain.absence.enums

import io.swagger.v3.oas.annotations.media.Schema

enum class AbsenceApprovedType(
    @Schema(description = "해당 출석 상태의 점수")
    val points: Int
) {
    @Schema(description = "사유서 인정(공결): 0점")
    EXCUSED(0),

    @Schema(description = "결석(미제출): -3점")
    ABSENT(-3),

    @Schema(description = "결석(사유서 제출): -2점")
    ABSENT_WITH_DOC(-2),

    @Schema(description = "기타 사유(사유서 제출): -1점")
    ABSENT_WITH_CAUSE(-1),

    @Schema(description = "지각: -1점")
    LATE(-1),

    @Schema(description = "조퇴: -1점")
    EARLY_LEAVE(-1)
}