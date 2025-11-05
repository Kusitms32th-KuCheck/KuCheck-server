package onku.backend.domain.absence.enums

import io.swagger.v3.oas.annotations.media.Schema

enum class AbsenceApprovedType {
    @Schema(description = "사유서 인정(공결): 0점")
    EXCUSED,

    @Schema(description = "결석(미제출): -3점")
    ABSENT,

    @Schema(description = "결석(사유서 제출): -2점")
    ABSENT_WITH_DOC,

    @Schema(description = "기타 사유(사유서 제출): -1점")
    ABSENT_WITH_CAUSE,

    @Schema(description = "지각: -1점")
    LATE,

    @Schema(description = "조퇴: -1점")
    EARLY_LEAVE
}