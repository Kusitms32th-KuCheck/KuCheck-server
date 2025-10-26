package onku.backend.domain.member.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.dto.MemberApprovalResponse
import onku.backend.domain.member.dto.UpdateApprovalRequest
import onku.backend.domain.member.Member
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.service.MemberProfileService
import onku.backend.domain.member.service.MemberService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members")
@Tag(name = "회원 API", description = "온보딩 및 프로필 관련 API")
class MemberController(
    private val memberProfileService: MemberProfileService,
    private val s3Service: S3Service,
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
        description = "현재 로그인한 회원의 이름(name), 파트(part), 상벌점 총합(totalPoints) 반환"
    )
    fun getMyProfileSummary(
        @CurrentMember member: Member
    ): SuccessResponse<MemberProfileResponse> {
        val body = memberProfileService.getProfileSummary(member)
        return SuccessResponse.ok(body)
    }

    @PatchMapping("/profile/image")
    @Operation(
        summary = "내 프로필 이미지 수정",
        description = "이미지 URL을 입력받아 MemberProfile.profileImage를 수정하고 최종 URL을 반환"
    )
    fun updateMyProfileImage(
        @CurrentMember member: Member,
        @RequestBody @Valid req: UpdateProfileImageRequest
    ): SuccessResponse<UpdateProfileImageResponse> {
        val body = memberProfileService.updateProfileImage(member, req)
        return SuccessResponse.ok(body)
    }

    @GetMapping("/profile/image/url")
    @Operation(
        summary = "프로필 이미지 업로드용 Presigned URL 발급",
        description = "업로드 URL 반환"
    )
    fun profileImagePostUrl(
        @CurrentMember member: Member,
        @RequestParam fileName: String
    ): ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        val dto = s3Service.getPostS3Url(
            memberId = member.id!!,
            filename = fileName,
            folderName = FolderName.MEMBER_PROFILE.name,
            option = UploadOption.IMAGE
        )
        return ResponseEntity.ok(SuccessResponse.ok(GetPreSignedUrlDto(preSignedUrl = dto.preSignedUrl)))
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
}