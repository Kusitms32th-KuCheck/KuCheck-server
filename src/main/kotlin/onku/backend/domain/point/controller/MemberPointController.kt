package onku.backend.domain.point.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.member.Member
import onku.backend.domain.point.dto.UserPointHistoryResponse
import onku.backend.domain.point.service.MemberPointService
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
    private val memberPointService: MemberPointService
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
    ): ResponseEntity<SuccessResponse<UserPointHistoryResponse>> {
        val body = memberPointService.getHistory(member, page, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}
