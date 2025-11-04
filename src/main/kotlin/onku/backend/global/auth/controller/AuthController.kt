package onku.backend.global.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.member.Member
import onku.backend.domain.member.service.MemberService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.auth.dto.AuthLoginResult
import onku.backend.global.auth.dto.KakaoLoginRequest
import onku.backend.global.auth.service.AuthService
import onku.backend.global.auth.service.KakaoService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "인증 API", description = "소셜 로그인 및 토큰 재발급")
class AuthController(
    private val authService: AuthService,
    private val kakaoService: KakaoService,
    private val memberService: MemberService,
) {
    @PostMapping("/kakao")
    @Operation(summary = "카카오 로그인", description = "인가코드를 body로 받아 사용자를 식별합니다.")
    fun kakaoLogin(@RequestBody req: KakaoLoginRequest): ResponseEntity<SuccessResponse<AuthLoginResult>> =
        authService.kakaoLogin(req)

    @PostMapping("/reissue")
    @Operation(summary = "AT 재발급", description = "RT를 헤더로 받아 AT를 재발급합니다.")
    fun reissue(@RequestHeader("X-Refresh-Token") refreshToken: String): ResponseEntity<SuccessResponse<String>> =
        authService.reissueAccessToken(refreshToken)

    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "로그아웃 처리를 위해 X-Refresh-Token 헤더로 받아온 RT를 서버 저장소(REDIS)에서 삭제합니다."
    )
    fun logout(@RequestHeader("X-Refresh-Token") refreshToken: String): ResponseEntity<SuccessResponse<String>> =
        authService.logout(refreshToken)

    @PostMapping("/withdraw")
    @Operation(
        summary = "회원 탈퇴",
        description = "카카오 unlink 요청 + DB 회원 삭제 + RT 삭제"
    )
    fun withdraw(
        @CurrentMember member: Member,
        @RequestHeader("X-Refresh-Token") refreshToken: String
    ): ResponseEntity<SuccessResponse<String>> =
        authService.withdraw(member, refreshToken)
}