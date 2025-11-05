package onku.backend.domain.kupick.dto

import java.time.LocalDateTime

data class KupickFcmInfo(
    val fcmToken: String?,
    val submitDate: LocalDateTime?
)
