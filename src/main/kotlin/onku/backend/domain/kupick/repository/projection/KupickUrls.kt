package onku.backend.domain.kupick.repository.projection

import java.time.LocalDateTime

interface KupickUrls {
    fun getApplicationImageUrl(): String?
    fun getViewImageUrl(): String?
    fun getApplicationDate(): LocalDateTime?
    fun getViewDate(): LocalDateTime?
}