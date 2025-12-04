package onku.backend.domain.session.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import java.time.LocalTime

@Schema(description = "세션 시간 수정 결과")
data class SessionTimeResetResponse(

    @Schema(description = "세션 ID", example = "1")
    val sessionId: Long,

    @Schema(description = "수정된 세션 시작 시간", example = "19:20:00")
    val startTime: LocalTime,

    @Schema(description = "수정된 세션 종료 시간", example = "21:20:00")
    val endTime: LocalTime,

    @Schema(description = "출석 확정 여부", example = "false")
    val attendanceFinalized: Boolean,

    @Schema(description = "출석 확정 시각 (null로 초기화)", example = "null")
    val attendanceFinalizedAt: LocalDateTime?
)
