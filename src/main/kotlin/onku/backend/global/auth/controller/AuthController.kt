package onku.backend.global.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.auth.service.AuthService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.HttpStatus
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
    fun kakaoLogin(@RequestBody @Valid dto: KakaoLoginRequest): ResponseEntity<SuccessResponse<Any?>> {
        val (result, headers) = authService.kakaoLogin(dto)

        return when (result.status) {
            ApprovalStatus.APPROVED -> {
                ResponseEntity.ok()
                    .apply {
                        headers.accessToken?.let { header("Authorization", "Bearer $it") }
                        headers.refreshToken?.let { header("Refresh-Token", it) }
                    }
                    .body(SuccessResponse.ok(result))
            }

            ApprovalStatus.PENDING -> {
                if (headers.onboardingToken != null) {
                    ResponseEntity.ok()
                        .header("Authorization", "Bearer ${headers.onboardingToken}")
                        .body(SuccessResponse.ok(result))
                } else {
                    ResponseEntity.status(HttpStatus.ACCEPTED)
                        .body(SuccessResponse.ok(result))
                }
            }

            ApprovalStatus.REJECTED -> {
                ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(SuccessResponse.ok(result))
            }
        }
    }

    @PostMapping("/reissue")
    @Operation(summary = "AT 재발급", description = "RT를 헤더로 받아 AT를 재발급합니다.")
    fun reissue(@RequestHeader("Refresh-Token") refreshToken: String): ResponseEntity<SuccessResponse<String>> {
        val newAccess = authService.reissueAccessToken(refreshToken)
        return ResponseEntity.ok()
            .header("Authorization", "Bearer $newAccess")
            .body(SuccessResponse.ok("Access Token이 재발급되었습니다."))
    }
}
