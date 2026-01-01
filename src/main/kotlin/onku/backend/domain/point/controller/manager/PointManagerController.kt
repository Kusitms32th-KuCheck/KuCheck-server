package onku.backend.domain.point.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import onku.backend.domain.point.dto.*
import onku.backend.domain.point.service.AdminPointCommandService
import onku.backend.domain.point.service.AdminPointService
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/v1/points/manage")
@Tag(
    name = "[MANAGEMENT] 운영진 상벌점",
    description = "운영진 상벌점 대시보드 조회 API"
)
@Validated
class PointManagerController(
    private val adminPointsService: AdminPointService,
    private val commandService: AdminPointCommandService
) {

    @GetMapping("/overview")
    @Operation(
        summary = "운영진 상벌점 목록 조회",
        description = "이름, 파트, 월별 출결 점수(출결만 반영), 월별 큐픽 참여 여부, TF/운영진 여부, 연락/학교/학과, 스터디/큐포터즈 점수, 메모를 멤버 단위로 페이징하여 반환"
    )
    fun overview(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<SuccessResponse<PageResponse<AdminPointOverviewDto>>> {
        val safePage = if (page < 1) 0 else page - 1
        val year = 2025
        val body = adminPointsService.getAdminOverview(year, safePage, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PatchMapping("/study")
    @Operation(summary = "스터디 점수 수정", description = "memberId와 studyPoints를 받아 수정합니다.")
    fun updateStudyPoints(@RequestBody @Valid req: UpdateStudyPointsRequest)
            : ResponseEntity<SuccessResponse<StudyPointsResult>> {
        val result = commandService.updateStudyPoints(req.memberId!!, req.studyPoints!!)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/kupporters")
    @Operation(summary = "큐포터즈 점수 수정", description = "memberId와 kupportersPoints를 받아 수정합니다.")
    fun updateKupportersPoints(@RequestBody @Valid req: UpdateKupportersPointsRequest)
            : ResponseEntity<SuccessResponse<KupportersPointsResult>> {
        val result = commandService.updateKupportersPoints(req.memberId!!, req.kuportersPoints!!)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/memo")
    @Operation(summary = "메모 수정", description = "memberId와 memo를 받아 수정합니다.")
    fun updateMemo(@RequestBody @Valid req: UpdateMemoRequest)
            : ResponseEntity<SuccessResponse<MemoResult>> {
        val result = commandService.updateMemo(req.memberId!!, req.memo!!)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/is-tf")
    @Operation(summary = "TF 여부 토글", description = "memberId만 받아 현재 TF 여부를 반전시킵니다.")
    fun updateIsTf(@RequestBody @Valid req: ToggleMemberRequest)
            : ResponseEntity<SuccessResponse<IsTfResult>> {
        val isTf = commandService.updateIsTf(req.memberId!!)
        val result = IsTfResult(memberId = req.memberId!!, isTf = isTf)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/is-staff")
    @Operation(summary = "운영진(Staff) 여부 토글", description = "memberId만 받아 현재 Staff 여부를 반전시킵니다.")
    fun updateIsStaff(@RequestBody @Valid req: ToggleMemberRequest)
            : ResponseEntity<SuccessResponse<IsStaffResult>> {
        val isStaff = commandService.updateIsStaff(req.memberId!!)
        val result = IsStaffResult(memberId = req.memberId!!, isStaff = isStaff)
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @PatchMapping("/kupick")
    @Operation(
        summary = "이번 달 큐픽 제출여부 수정 토글",
        description = "memberId, YearMonth값을 받아 이번 달 큐픽 승인 여부를 반전시킵니다. 제출 레코드가 없으면 생성합니다."
    )
    fun updateKupick(@RequestBody @Valid req: ToggleMemberRequest)
            : ResponseEntity<SuccessResponse<KupickApprovalResult>> {
        val ym = YearMonth.parse(req.yearMonth)
        val result = commandService.updateKupickApproval(
            memberId = req.memberId,
            targetYm = ym
        )
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }

    @GetMapping("/monthly")
    @Operation(
        summary = "월간 출석 현황 [운영진]",
        description = "year, month, page, size를 받아 멤버별 [date, attendanceId, status, point] 목록을 페이징으로 반환"
    )
    fun getMonthly(
        @RequestParam @Min(8) @Max(12) month: Int,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<SuccessResponse<MonthlyAttendancePageResponse>> {
        val safePage = if (page < 1) 0 else page - 1
        val year = 2025
        val body = adminPointsService.getMonthlyPaged(year, month, safePage, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PatchMapping("/monthly")
    @Operation(
        summary = "월별 출석 상태 수정 [운영진]",
        description = "attendanceId, memberId, status를 받아 출석 상태를 수정하고, 상벌점 변동을 기록합니다."
    )
    fun updateMonthly(
        @RequestBody @Valid req: UpdateAttendanceStatusRequest
    ): ResponseEntity<SuccessResponse<UpdateAttendanceStatusResponse>> {

        val result = commandService.updateAttendanceAndHistory(
            attendanceId = req.attendanceId,
            memberId = req.memberId,
            newStatus = req.status
        )
        return ResponseEntity.ok(SuccessResponse.ok(result))
    }
}