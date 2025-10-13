package onku.backend.domain.attendance.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.attendance.dto.AttendanceRequest
import onku.backend.domain.attendance.dto.AttendanceResponse
import onku.backend.domain.attendance.dto.AttendanceTokenResponse
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
    private val attendanceService: AttendanceService
) {

    @PostMapping("/token")
    @Operation(summary = "출석용 토큰 발급 [USER]", description = "15초 유효")
    fun issueQrToken(@CurrentMember member: Member): ResponseEntity<SuccessResponse<AttendanceTokenResponse>> {
        val headers = HttpHeaders().apply { add(HttpHeaders.CACHE_CONTROL, "no-store") }
        return ResponseEntity.ok().headers(headers).body(SuccessResponse.ok(attendanceService.issueAttendanceTokenFor(member)))
    }

    @PostMapping("/scan")
    @Operation(summary = "출석 스캔 [ADMIN]", description = "열린 세션 자동 선택 → 토큰 검증 & 소비 → insert")
    fun scan(@CurrentMember admin: Member, @RequestBody req: AttendanceRequest): SuccessResponse<AttendanceResponse> {
        return SuccessResponse.ok(attendanceService.scanAndRecordBy(admin, req.token))
    }
}
