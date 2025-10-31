package onku.backend.domain.attendance.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.attendance.dto.AttendanceRequest
import onku.backend.domain.attendance.dto.AttendanceResponse
import onku.backend.domain.attendance.dto.AttendanceTokenResponse
import onku.backend.domain.attendance.dto.WeeklyAttendanceSummary
import onku.backend.domain.attendance.facade.AttendanceFacade
import onku.backend.domain.attendance.service.AttendanceService
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/attendance")
@Tag(name = "출석 API")
class AttendanceController(
    private val attendanceService: AttendanceService,
    private val attendanceFacade: AttendanceFacade
) {
    @PostMapping("/token")
    @Operation(summary = "출석용 토큰 발급 [USER]", description = "15초 유효 + 프로필(이름/파트/학교/프사) 포함")
    fun issueQrToken(@CurrentMember member: Member): ResponseEntity<SuccessResponse<AttendanceTokenResponse>> {
        val headers = HttpHeaders().apply { add(HttpHeaders.CACHE_CONTROL, "no-store") }
        val body = attendanceFacade.issueTokenWithProfile(member)
        return ResponseEntity.ok().headers(headers).body(SuccessResponse.ok(body))
    }

    @PostMapping("/scan")
    @Operation(summary = "출석 스캔 [MANAGEMENT]", description = "열린 세션 자동 선택 → 토큰 검증 & 소비 (반환값에 금주 출석 요약 조회를 추가했습니다. 출석 시 같이 업데이트해주시면 됩니다)")
    fun scan(@CurrentMember admin: Member, @RequestBody req: AttendanceRequest): SuccessResponse<AttendanceResponse> {
        return SuccessResponse.ok(attendanceService.scanAndRecordBy(admin, req.token))
    }


    @GetMapping("/weekly-summary")
    @Operation(
        summary = "금주 출석 요약 조회",
        description = "이번 주 기간 내 출석/조퇴/지각/결석 인원 반환. 출석 페이지 첫 로딩 때 호출해주시면 됩니다!"
    )
    fun getThisWeekSummary(): SuccessResponse<WeeklyAttendanceSummary> {
        val summary = attendanceService.getThisWeekSummary()
        return SuccessResponse.ok(summary)
    }
}
