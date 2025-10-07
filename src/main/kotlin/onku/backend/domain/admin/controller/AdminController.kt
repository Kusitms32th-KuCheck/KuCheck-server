package onku.backend.domain.admin.controller

import onku.backend.domain.admin.dto.UpdateApprovalRequest
import onku.backend.domain.admin.dto.MemberApprovalResponse
import onku.backend.domain.admin.service.AdminService
import onku.backend.global.response.SuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 API", description = "관리자 권한용 API")
@RestController
@RequestMapping("/api/v1/admin/members")
class AdminController(
    private val adminService: AdminService
) {

    @Operation(
        summary = "[관리자] 회원 승인 상태 변경",
        description = "PENDING 상태의 회원만 승인/거절할 수 있습니다. (PENDING → APPROVED/REJECTED)"
    )
    @PatchMapping("/{memberId}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateApproval(
        @PathVariable memberId: Long,
        @RequestBody @Valid body: UpdateApprovalRequest
    ): ResponseEntity<SuccessResponse<MemberApprovalResponse>> {
        val result = adminService.updateApproval(memberId, body.status)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }
}
