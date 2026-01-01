package onku.backend.global.auth.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoProfile(
    @JsonProperty("id") val id: Long,
    @JsonProperty("kakao_account") val kakaoAccount: KakaoAccount?
)