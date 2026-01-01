package onku.backend.domain.session.repository.projection

import java.time.LocalDate
import java.time.LocalTime

interface ThisWeekSessionProjection {
    val sessionId: Long
    val sessionDetailId: Long?
    val title: String?
    val place: String?
    val startDate: LocalDate?
    val startTime: LocalTime?
    val endTime: LocalTime?
}