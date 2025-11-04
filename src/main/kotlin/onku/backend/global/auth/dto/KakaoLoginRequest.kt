package onku.backend.global.auth.dto

import onku.backend.global.auth.enums.KakaoEnv

data class KakaoLoginRequest(
    val code: String,
    val env: KakaoEnv
)