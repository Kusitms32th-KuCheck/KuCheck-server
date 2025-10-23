package onku.backend.domain.session.dto.request

import com.fasterxml.jackson.annotation.JsonFormat
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
    val startTime : LocalTime,
    @field:NotNull
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    val endTime : LocalTime,
    @field:NotBlank val content: String,
)
