package onku.backend.domain.attendance.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.attendance.dto.*
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
class AttendanceMemberController(
    private val attendanceService: AttendanceService,
    private val attendanceFacade: AttendanceFacade
) {

    @PostMapping("/token")
    @Operation(
        summary = "출석용 토큰 발급 [USER]",
        description = "15초 유효 + 프로필(이름/파트/학교/프사) 포함"
    )
    fun issueQrToken(@CurrentMember member: Member): ResponseEntity<SuccessResponse<AttendanceTokenResponse>> {
        val headers = HttpHeaders().apply { add(HttpHeaders.CACHE_CONTROL, "no-store") }
        val body = attendanceFacade.issueTokenWithProfile(member)
        return ResponseEntity.ok().headers(headers).body(SuccessResponse.ok(body))
    }

    @GetMapping("/availability")
    @Operation(
        summary = "지금 출석 가능 여부 확인 [USER]",
        description = "열린 세션 존재 및 기존 출석 기록 유무를 확인해 가능 여부 반환"
    )
    fun checkAvailability(@CurrentMember member: Member): SuccessResponse<AttendanceAvailabilityResponse> {
        val body = attendanceService.checkAvailabilityFor(member)
        return SuccessResponse.ok(body)
    }
}