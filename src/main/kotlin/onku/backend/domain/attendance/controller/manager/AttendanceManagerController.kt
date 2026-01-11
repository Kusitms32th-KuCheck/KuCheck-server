package onku.backend.domain.attendance.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.attendance.dto.*
import onku.backend.domain.attendance.service.AttendanceService
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/attendance/manage")
@Tag(name = "[MANAGEMENT] 출석 API")
class AttendanceManagerController(
    private val attendanceService: AttendanceService
) {

    @PostMapping("/scan")
    @Operation(
        summary = "출석 스캔 [MANAGEMENT]",
        description = "열린 세션 자동 선택 → 토큰 검증 & 소비 (반환값에 금주 출석 요약 포함)"
    )
    fun scan(@CurrentMember admin: Member, @RequestBody req: AttendanceRequest): SuccessResponse<AttendanceResponse> {
        return SuccessResponse.ok(attendanceService.scanAndRecordBy(admin, req.token))
    }

    @GetMapping("/weekly-summary")
    @Operation(
        summary = "금주 출석 요약 조회 [MANAGEMENT]",
        description = "이번 주 기간 내 출석/조퇴/지각/결석 인원 반환"
    )
    fun getThisWeekSummary(): SuccessResponse<WeeklyAttendanceSummary> {
        val summary = attendanceService.getThisWeekSummary()
        return SuccessResponse.ok(summary)
    }
}