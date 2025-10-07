package onku.backend.global.auth.service

import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.dto.KakaoOAuthTokenResponse
import onku.backend.global.auth.dto.KakaoProfile
import onku.backend.global.exception.CustomException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient

@Service
class KakaoService(
    @Value("\${oauth.kakao.client-id}") private val clientId: String,
    @Value("\${oauth.kakao.redirect-uri}") private val redirectUri: String
) {
    private val client = RestClient.create()

    fun getAccessToken(code: String): KakaoOAuthTokenResponse {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", clientId)
            add("redirect_uri", redirectUri)
            add("code", code)
        }

        return try {
            val res: ResponseEntity<KakaoOAuthTokenResponse> = client.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(params)
                .retrieve()
                .toEntity(KakaoOAuthTokenResponse::class.java)

            res.body ?: throw CustomException(AuthErrorCode.KAKAO_TOKEN_EMPTY_RESPONSE)
        } catch (e: Exception) {
            throw CustomException(AuthErrorCode.KAKAO_API_COMMUNICATION_ERROR)
        }
    }

    fun getProfile(accessToken: String): KakaoProfile {
        return try {
            val res: ResponseEntity<KakaoProfile> = client.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .toEntity(KakaoProfile::class.java)

            res.body ?: throw CustomException(AuthErrorCode.KAKAO_PROFILE_EMPTY_RESPONSE)
        } catch (e: Exception) {
            throw CustomException(AuthErrorCode.KAKAO_API_COMMUNICATION_ERROR)
        }
    }
}
