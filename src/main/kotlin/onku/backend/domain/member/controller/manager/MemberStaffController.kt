package onku.backend.domain.member.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.service.MemberProfileService
import onku.backend.domain.member.service.MemberService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members/staff")
@Tag(name = "[STAFF] 회원 API", description = "운영진용 회원 관리 API")
class MemberStaffController(
    private val memberProfileService: MemberProfileService,
    private val memberService: MemberService
) {

    @Operation(
        summary = "회원 승인 상태 일괄 변경",
        description = "PENDING 상태의 회원만 승인/거절할 수 있습니다. (PENDING → APPROVED/REJECTED)"
    )
    @PatchMapping("/approvals")
    fun updateApproval(
        @RequestBody @Valid body: List<UpdateApprovalRequest>
    ): ResponseEntity<SuccessResponse<List<MemberApprovalResponse>>> {
        val result = memberService.updateApprovals(body)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/{memberId}/profile")
    @Operation(
        summary = "학회원 프로필 정보 수정",
        description = "관리자가 특정 학회원의 [이름, 학교, 학과, 전화번호, 파트]를 수정합니다."
    )
    fun updateMemberProfile(
        @PathVariable memberId: Long,
        @RequestBody @Valid req: MemberProfileUpdateRequest
    ): SuccessResponse<MemberProfileBasicsResponse> {
        val body = memberProfileService.updateProfile(memberId, req)
        return SuccessResponse.ok(body)
    }

    @GetMapping("/requests")
    @Operation(
        summary = "승인 요청 목록 조회 (PENDING/REJECTED)",
        description = "PENDING/APPROVED/REJECTED 수와 함께, PENDING 및 REJECTED 상태인 학회원들만 페이징하여 반환합니다."
    )
    fun getApprovalRequests(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): SuccessResponse<MembersPagedResponse> {
        val safePage = if (page < 1) 0 else page - 1
        val body = memberProfileService.getApprovalRequestMembers(safePage, size)
        return SuccessResponse.ok(body)
    }


    @GetMapping("/approved")
    @Operation(
        summary = "승인된 회원 명단 페이징 조회",
        description = "APPROVED 상태인 회원의 id, 이름, 파트, 학교, 학과, 전화번호, 소셜정보, 이메일, role, isStaff 를 페이징하여 반환합니다." +
        "isStaff 파라미터로 운영진/비운영진 필터링이 가능하며," +
        "상단에는 PENDING/APPROVED/REJECTED 회원 수가 각각 제공됩니다."
    )
    fun getApprovedMembersPaged(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) isStaff: Boolean?
    ): SuccessResponse<MembersPagedResponse> {
        val safePage = if (page < 1) 0 else page - 1
        val body = memberProfileService.getApprovedMembersPagedWithCounts(safePage, size, isStaff)
        return SuccessResponse.ok(body)
    }
}
