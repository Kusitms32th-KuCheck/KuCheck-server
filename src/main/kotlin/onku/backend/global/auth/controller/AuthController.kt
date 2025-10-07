package onku.backend.global.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.global.auth.dto.AuthLoginResult
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.auth.service.AuthService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "소셜 로그인 및 토큰 재발급")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "인가코드를 body로 받아 사용자를 식별합니다.")
    fun kakaoLogin(@RequestBody req: KakaoLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>> =
        authService.kakaoLogin(req)

    @PostMapping("/reissue")
    @Operation(summary = "AT 재발급", description = "RT를 헤더로 받아 AT를 재발급합니다.")
    fun reissue(@RequestHeader("X-Refresh-Token") refreshToken: String): ResponseEntity<SuccessResponse<String>> =
        authService.reissueAccessToken(refreshToken)
}