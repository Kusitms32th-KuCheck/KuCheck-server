package onku.backend.domain.session.dto.request

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import onku.backend.domain.session.annotation.SessionValidTimeRange
import java.time.LocalTime
@SessionValidTimeRange
data class UpsertSessionDetailRequest(
    val sessionDetailId : Long?,
    @field:NotNull val sessionId : Long,
    @field:NotBlank val place : String,
    @field:NotNull
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "시작시각", example = "14:00:00")
    val startTime : LocalTime,
    @field:NotNull
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    @Schema(description = "종료시각", example = "14:00:00")
    val endTime : LocalTime,
    @field:NotBlank val content: String,
)
