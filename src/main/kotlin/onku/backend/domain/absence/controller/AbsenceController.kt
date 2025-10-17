package onku.backend.domain.absence.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.facade.AbsenceFacade
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/absence")
@Tag(name = "불참사유서 관련 API")
class AbsenceController(
    private val absenceFacade : AbsenceFacade
) {
    @PostMapping("")
    @Operation(summary = "불참 사유서 제출", description = "불참 사유서 제출하는 API 입니다")
    fun submitAttendanceReport(@CurrentMember member: Member,
                               @RequestBody submitAbsenceReportRequest: SubmitAbsenceReportRequest): ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(absenceFacade.submitAbsenceReport(member, submitAbsenceReportRequest)))
    }
}