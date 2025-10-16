package onku.backend.domain.session.dto

data class SessionAboutAbsenceResponse(
    val sessionId : Long?,
    val title : String,
    val week : Long,
    val active : Boolean
)
