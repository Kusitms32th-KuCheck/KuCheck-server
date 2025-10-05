package onku.backend.domain.member.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.dto.OnboardingRequest
import onku.backend.domain.member.dto.OnboardingResponse
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.service.MemberProfileService
import onku.backend.domain.member.service.MemberService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "회원 API", description = "온보딩 관련 API")
class MemberController(
    private val memberService: MemberService,
    private val memberProfileService: MemberProfileService
) {

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "온보딩 정보 제출",
        description = "소셜 로그인 시도 후 회원가입이 되지 않았고 온보딩을 완료하지 않은 회원에게 발급되는 온보딩 전용 토큰으로 접근 가능."
    )
    fun submitOnboarding(
        auth: Authentication,
        @RequestBody @Valid req: OnboardingRequest
    ): SuccessResponse<OnboardingResponse> {
        val email = auth.name
        val member = memberService.getByEmail(email)

        memberProfileService.createOrUpdateProfile(member.id!!, req)
        memberService.markOnboarded(member)

        val body = OnboardingResponse(
            status = ApprovalStatus.PENDING,
            message = "온보딩이 접수되었습니다. 운영진 승인 후 로그인할 수 있습니다."
        )
        return SuccessResponse.ok(body)
    }
}