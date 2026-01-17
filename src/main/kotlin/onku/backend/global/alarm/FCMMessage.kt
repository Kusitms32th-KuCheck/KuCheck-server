package onku.backend.global.alarm

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class FCMMessage(
    val validateOnly: Boolean,
    val message: Message
) {
    data class Message(
        val notification: Notification,
        val token: String,
        val webpush : Webpush?
    )

    data class Notification(
        val title: String,
        val body: String,
        val image: String?
    )
    @Serializable
    data class Webpush(
        @SerialName("fcm_options")
        val fcmOptions : FcmOptions?
    )

    @Serializable
    data class FcmOptions(
        val link : String?
    )
}