package onku.backend.domain.session.repository.projection

import java.time.LocalDateTime

interface SessionAboutAbsenceProjection {
    val id: Long?
    val title: String
    val week: Long
    val startTime: LocalDateTime
}