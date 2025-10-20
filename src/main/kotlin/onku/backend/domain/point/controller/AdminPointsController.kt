package onku.backend.domain.point.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.point.dto.*
import onku.backend.domain.point.service.AdminPointsCommandService
import onku.backend.domain.point.service.AdminPointsService
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/points")
@Tag(
    name = "운영진 상벌점",
    description = "운영진 상벌점 대시보드 조회 API"
)
class AdminPointsController(
    private val adminPointsService: AdminPointsService,
    private val commandService: AdminPointsCommandService
) {

    @GetMapping("/overview")
    @Operation(
        summary = "운영진 상벌점 목록 조회",
        description = "이름, 파트, 월별 출결 점수(출결만 반영), 월별 큐픽 참여 여부, TF/운영진 여부, 연락/학교/학과, 스터디/큐포터즈 점수, 메모를 멤버 단위로 페이징하여 반환"
    )
    fun overview(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<SuccessResponse<PageResponse<AdminPointsRowDto>>> {
        val safePage = if (page < 1) 0 else page - 1
        val year = 2025
        val body = adminPointsService.getAdminOverview(year, safePage, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PatchMapping("/study")
    @Operation(summary = "스터디 점수 수정", description = "memberId와 studyPoints를 받아 수정합니다.")
    fun updateStudyPoints(@RequestBody @Valid req: UpdateStudyPointsRequest): ResponseEntity<SuccessResponse<Unit>> {
        commandService.updateStudyPoints(req.memberId!!, req.studyPoints!!)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }

    @PatchMapping("/kupporters")
    @Operation(summary = "큐포터즈 점수 수정", description = "memberId와 kuportersPoints를 받아 수정합니다.")
    fun updateKupportersPoints(@RequestBody @Valid req: UpdateKupportersPointsRequest): ResponseEntity<SuccessResponse<Unit>> {
        commandService.updateKupportersPoints(req.memberId!!, req.kuportersPoints!!)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }

    @PatchMapping("/memo")
    @Operation(summary = "메모 수정", description = "memberId와 memo를 받아 수정합니다.")
    fun updateMemo(@RequestBody @Valid req: UpdateMemoRequest): ResponseEntity<SuccessResponse<Unit>> {
        commandService.updateMemo(req.memberId!!, req.memo!!)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }

    @PatchMapping("/is-tf")
    @Operation(summary = "TF 여부 수정", description = "memberId와 isTf를 받아 Member의 isTf를 수정합니다.")
    fun updateIsTf(@RequestBody @Valid req: UpdateIsTfRequest): ResponseEntity<SuccessResponse<Unit>> {
        commandService.updateIsTf(req.memberId!!, req.isTf!!)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }
}
