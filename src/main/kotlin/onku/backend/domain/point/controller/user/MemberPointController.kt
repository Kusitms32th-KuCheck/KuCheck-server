package onku.backend.domain.point.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.member.Member
import onku.backend.domain.point.dto.MemberPointHistoryResponse
import onku.backend.domain.point.service.MemberPointHistoryService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/points")
@Tag(
    name = "사용자 상벌점",
    description = "사용자 상벌점 조회 API"
)
class MemberPointController(
    private val memberPointService: MemberPointHistoryService
) {
    @GetMapping("/history")
    @Operation(
        summary = "사용자 상/벌점 이력 조회",
        description = "회원의 상/벌점 누적 합 및 날짜순(최신순) 이력을 페이징하여 반환"
    )
    fun history(
        @CurrentMember member: Member,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<SuccessResponse<MemberPointHistoryResponse>> {
        val safePage = if (page < 1) 0 else page - 1
        val body = memberPointService.getHistory(member, safePage, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}
