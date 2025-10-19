package onku.backend.domain.attendance.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "출석 상태")
enum class AttendanceStatus {

    @Schema(description = "출석(공휴일 세션): +1점")
    PRESENT_HOLIDAY,

    @Schema(description = "출석(정시): 0점")
    PRESENT,

    @Schema(description = "사유서 인정(공결): 0점")
    EXCUSED,

    @Schema(description = "결석(미제출): -3점")
    ABSENT,

    @Schema(description = "결석(사유서 제출): -2점")
    ABSENT_WITH_DOC,

    @Schema(description = "지각: -1점")
    LATE
}
