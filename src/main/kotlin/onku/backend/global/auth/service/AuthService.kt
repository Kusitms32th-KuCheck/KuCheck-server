package onku.backend.global.auth.service

import onku.backend.global.auth.dto.AuthHeaders
import onku.backend.global.auth.dto.AuthLoginResult
import onku.backend.global.auth.dto.KakaoLoginRequest

interface AuthService {
    fun reissueAccessToken(refreshToken: String): String
    fun kakaoLogin(dto: KakaoLoginRequest): Pair<AuthLoginResult, AuthHeaders>
}