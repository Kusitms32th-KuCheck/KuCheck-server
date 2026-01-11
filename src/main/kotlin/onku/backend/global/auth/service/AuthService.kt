package onku.backend.global.auth.service

import onku.backend.domain.member.Member
import onku.backend.global.auth.dto.AppleLoginRequest
import onku.backend.global.auth.dto.AuthLoginResult
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity

interface AuthService {
    fun kakaoLogin(dto: KakaoLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>>
    fun appleLogin(dto: AppleLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>>
    fun reissueAccessToken(refreshToken: String): ResponseEntity<SuccessResponse<String>>
    fun logout(refreshToken: String): ResponseEntity<SuccessResponse<String>>
    fun withdraw(member: Member, refreshToken: String): ResponseEntity<SuccessResponse<String>>
}

