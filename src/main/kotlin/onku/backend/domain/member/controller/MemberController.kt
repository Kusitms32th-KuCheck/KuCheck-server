package onku.backend.domain.member.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.Member
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.service.MemberProfileService
import onku.backend.domain.member.service.MemberService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetUpdateAndDeleteUrlDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "회원 API", description = "온보딩 및 프로필 관련 API")
class MemberController(
    private val memberProfileService: MemberProfileService,
    private val memberService: MemberService
) {

    @PostMapping("/onboarding")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
        summary = "온보딩 정보 제출",
        description = "온보딩 전용 토큰으로 접근"
    )
    fun submitOnboarding(
        @CurrentMember member: Member,
        @RequestBody @Valid req: OnboardingRequest
    ): SuccessResponse<OnboardingResponse> {
        val body = memberProfileService.submitOnboarding(member, req)
        return SuccessResponse.ok(body)
    }

    @GetMapping("/profile/summary")
    @Operation(
        summary = "내 프로필 요약 조회",
        description = "현재 로그인한 회원의 이름(name), 파트(part), 상벌점 총합(totalPoints), 프로필 이미지(profileImage) 반환"
    )
    fun getMyProfileSummary(
        @CurrentMember member: Member
    ): SuccessResponse<MemberProfileResponse> {
        val body = memberProfileService.getProfileSummary(member)
        return SuccessResponse.ok(body)
    }

    @GetMapping("/profile/image/url")
    @Operation(
        summary = "프로필 이미지 업로드용 Presigned URL 발급",
        description = "URL 발급과 동시에 DB에 프로필 이미지 key를 저장 or 갱신하며, 이전 이미지 삭제용 URL도 함께 반환"
    )
    fun profileImagePostUrl(
        @CurrentMember member: Member,
        @RequestParam fileName: String
    ): ResponseEntity<SuccessResponse<GetUpdateAndDeleteUrlDto>> {

        val dto = memberProfileService.issueProfileImageUploadUrl(member, fileName)
        return ResponseEntity.ok(SuccessResponse.ok(dto))
    }

    @Operation(
        summary = "[STAFF] 회원 승인 상태 변경",
        description = "PENDING 상태의 회원만 승인/거절할 수 있습니다. (PENDING → APPROVED/REJECTED)"
    )
    @PatchMapping("/{memberId}/approval")
    fun updateApproval(
        @PathVariable memberId: Long,
        @RequestBody @Valid body: UpdateApprovalRequest
    ): ResponseEntity<SuccessResponse<MemberApprovalResponse>> {
        val result = memberService.updateApproval(memberId, body.status)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/{memberId}/role")
    @Operation(
        summary = "[EXECUTIVE] 사용자 권한 수정",
        description = "URL 의 memberId 에 해당하는 사용자의 권한을 role 로 수정합니다."
    )
    fun updateRole(
        @PathVariable memberId: Long,
        @RequestBody @Valid req: UpdateRoleRequest
    ): ResponseEntity<SuccessResponse<MemberRoleResponse>> {
        val body = memberService.updateRole(memberId, req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PatchMapping("/{memberId}/profile")
    @Operation(
        summary = "[STAFF] 학회원 프로필 정보 수정",
        description = "관리자가 특정 학회원의 [이름, 학교, 학과, 전화번호, 파트]를 수정합니다."
    )
    fun updateMemberProfile(
        @PathVariable memberId: Long,
        @RequestBody @Valid req: MemberProfileUpdateRequest
    ): SuccessResponse<MemberProfileBasicsResponse> {
        val body = memberProfileService.updateProfile(memberId, req)
        return SuccessResponse.ok(body)
    }

    @GetMapping("/approvals")
    @Operation(
        summary = "[STAFF] 학회원 정보 목록 조회 (APPROVED)",
        description = "PENDING/APPROVED/REJECTED 수와 함께, APPROVED 상태인 학회원들만 목록으로 반환합니다."
    )
    fun getApprovedMembers(
    ): SuccessResponse<MemberInfoListResponse> {
        val body = memberProfileService.getApprovedMemberInfos()
        return SuccessResponse.ok(body)
    }

    @GetMapping("/requests")
    @Operation(
        summary = "[STAFF] 승인 요청 목록 조회 (PENDING/REJECTED)",
        description = "PENDING/APPROVED/REJECTED 수와 함께, PENDING 및 REJECTED 상태인 학회원들만 목록으로 반환합니다."
    )
    fun getApprovalRequests(
    ): SuccessResponse<MemberApprovalListResponse> {
        val body = memberProfileService.getApprovalRequestMembers()
        return SuccessResponse.ok(body)
    }
}