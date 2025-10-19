package onku.backend.domain.attendance.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "출석 상태",
    example = "PRESENT"
)
enum class AttendanceStatus {
    @Schema(description = "출석(정시)") PRESENT,
    @Schema(description = "결석") ABSENT,
    @Schema(description = "지각") LATE
}