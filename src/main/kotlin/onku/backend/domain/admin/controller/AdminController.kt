package onku.backend.domain.admin.controller

import onku.backend.domain.admin.dto.UpdateApprovalRequest
import onku.backend.domain.admin.service.AdminService
import onku.backend.domain.admin.dto.MemberApprovalResponse
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.global.response.ErrorResponse
import onku.backend.global.response.SuccessResponse

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@Tag(name = "관리자 API")
@RestController
@RequestMapping("/api/v1/admin/members")
class AdminController(
    private val adminService: AdminService
) {

    @Operation(
        summary = "[관리자] 회원 승인 상태 변경 (PENDING → APPROVED/REJECTED)",
        description = "PENDING 상태의 회원만 승인/거절할 수 있습니다."
    )
    @PatchMapping("/{memberId}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateApproval(
        @PathVariable memberId: Long,
        @RequestBody @Valid body: UpdateApprovalRequest
    ): ResponseEntity<Any> {

        if (body.status == ApprovalStatus.PENDING) {
            val error = ErrorResponse.of<Nothing>(
                code = "INVALID_REQUEST",
                message = "PENDING 으로 변경할 수 없습니다."
            )
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error)
        }

        val result: MemberApprovalResponse = adminService.updateApproval(memberId, body.status)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }
}
