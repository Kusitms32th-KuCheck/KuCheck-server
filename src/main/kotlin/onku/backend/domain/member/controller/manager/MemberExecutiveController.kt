package onku.backend.domain.member.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.dto.BulkRoleUpdateRequest
import onku.backend.domain.member.dto.MemberRoleResponse
import onku.backend.domain.member.dto.StaffUpdateRequest
import onku.backend.domain.member.dto.StaffUpdateResponse
import onku.backend.domain.member.service.MemberService
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/members/executive")
@Tag(name = "[EXECUTIVE] 회원 API", description = "회장단용 운영진 관리 API")
class MemberExecutiveController(
    private val memberService: MemberService
) {

    @PatchMapping("/staff")
    @Operation(
        summary = "운영진 여부 일괄 수정",
        description = "체크박스에 체크된 멤버 ID 리스트를 받아 운영진 여부를 일괄 수정합니다." +
        "- isStaff = true → false : role.USER 로 변경" +
        "- isStaff = false → true : role.STAFF 로 변경"
    )
    fun updateStaffMembers(
        @RequestBody @Valid req: StaffUpdateRequest
    ): ResponseEntity<SuccessResponse<StaffUpdateResponse>> {
        val body = memberService.updateStaffMembers(req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PatchMapping("/roles")
    @Operation(
        summary = "운영진 권한 일괄 수정",
        description = "여러 회원의 memberId, role 목록을 받아 한 번에 권한을 수정합니다. GUEST/USER 로의 변경은 허용하지 않습니다."
    )
    fun updateRoles(
        @RequestBody @Valid req: BulkRoleUpdateRequest
    ): ResponseEntity<SuccessResponse<List<MemberRoleResponse>>> {
        val body = memberService.updateRoles(req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}
