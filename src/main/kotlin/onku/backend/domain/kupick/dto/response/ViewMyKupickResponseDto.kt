package onku.backend.domain.kupick.dto.response

import java.time.LocalDateTime

data class ViewMyKupickResponseDto(
    val applicationUrl: String?,
    val applicationDateTime : LocalDateTime?,
    val viewUrl: String?,
    val viewDateTime : LocalDateTime?
) {
    companion object {
        fun of(applicationUrl: String?,
               applicationDateTime: LocalDateTime?,
               viewUrl: String?,
               viewDateTime: LocalDateTime?
        ) = ViewMyKupickResponseDto(
            applicationUrl,
            applicationDateTime,
            viewUrl,
            viewDateTime
        )
    }
}