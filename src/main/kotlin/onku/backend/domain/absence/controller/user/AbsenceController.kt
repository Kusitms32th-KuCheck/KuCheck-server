package onku.backend.domain.absence.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.dto.response.GetMyAbsenceReportResponse
import onku.backend.domain.absence.facade.AbsenceFacade
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/absence")
@Tag(name = "불참사유서 관련 API")
class AbsenceController(
    private val absenceFacade : AbsenceFacade
) {
    @PostMapping("")
    @Operation(summary = "불참 사유서 제출", description = "불참 사유서 제출하는 API 입니다")
    fun submitAbsenceReport(@CurrentMember member: Member,
                            @RequestBody @Valid submitAbsenceReportRequest: SubmitAbsenceReportRequest): ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(absenceFacade.submitAbsenceReport(member, submitAbsenceReportRequest)))
    }

    @GetMapping("")
    @Operation(summary = "불참 사유서 제출내역 조회", description = "내가 낸 불참 사유서 제출내역을 조회합니다.")
    fun getMyAbsenceReport(@CurrentMember member: Member)
    : ResponseEntity<SuccessResponse<List<GetMyAbsenceReportResponse>>> {
        return ResponseEntity.ok(SuccessResponse.ok(absenceFacade.getMyAbsenceReport(member)))
    }
}