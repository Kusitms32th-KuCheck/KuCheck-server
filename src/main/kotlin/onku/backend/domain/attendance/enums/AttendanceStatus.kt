package onku.backend.domain.attendance.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "출석 상태")
enum class AttendanceStatus(
    @Schema(description = "해당 출석 상태의 점수")
    val points: Int
) {
    @Schema(description = "출석(공휴일 세션): +1점")
    PRESENT_HOLIDAY(1),

    @Schema(description = "출석(정시): 0점")
    PRESENT(0),

    @Schema(description = "사유서 인정(공결): 0점")
    EXCUSED(0),

    @Schema(description = "결석(미제출): -3점")
    ABSENT(-3),

    @Schema(description = "결석(사유서 제출): -2점")
    ABSENT_WITH_DOC(-2),

    @Schema(description = "지각: -1점")
    LATE(-1);
}