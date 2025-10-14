package onku.backend.global.alarm

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class FCMService(
    private val objectMapper: ObjectMapper,
    @Value("\${fcm.api-url}")
    private val API_URL : String,
    @Value("\${fcm.firebase-config-path}")
    private val firebaseConfigPath : String,
    @Value("\${fcm.google.scope}")
    private val googleScope : String
) {
    private val log: Logger = LoggerFactory.getLogger(FCMService::class.java)
    companion object {
        private val client: OkHttpClient = OkHttpClient()
    }
    @Throws(IOException::class)
    fun sendMessageTo(targetToken: String, title: String, body: String, link: String?) {
        val message = makeMessage(targetToken, title, body, link)

        val requestBody = message
            .toRequestBody("application/json; charset=utf-8".toMediaType())

        val request = Request.Builder()
            .url(API_URL)
            .post(requestBody)
            .addHeader(HttpHeaders.AUTHORIZATION, "Bearer ${getAccessToken()}")
            .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
            .build()

        client.newCall(request).execute().use { response ->
            log.info("fcm 결과 : " + response.body?.string())
        }
    }

    @Throws(JsonProcessingException::class)
    private fun makeMessage(targetToken: String, title: String, body: String, link:String?): String {
        val fcmMessage = FCMMessage(
            validateOnly = false,
            message = FCMMessage.Message(
                token = targetToken,
                notification = FCMMessage.Notification(
                    title = title,
                    body = body,
                    image = null
                ),
                webpush = FCMMessage.Webpush(
                    FCMMessage.FcmOptions(link)
                )
            )
        )
        return objectMapper.writeValueAsString(fcmMessage)
    }

    @Throws(IOException::class)
    private fun getAccessToken(): String {
        try {
            val googleCredentials = GoogleCredentials
                .fromStream(ClassPathResource(firebaseConfigPath).inputStream)
                .createScoped(listOf(googleScope))

            googleCredentials.refreshIfExpired()
            return googleCredentials.accessToken.tokenValue
        } catch (e: IOException) {
            log.error("FCM AccessToken 발급 중 오류 발생", e)
            throw CustomException(ErrorCode.FCM_ACCESS_TOKEN_FAIL)
        }
    }
}