package onku.backend.domain.absence.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.absence.dto.request.EstimateAbsenceReportRequest
import onku.backend.domain.absence.dto.response.GetMemberAbsenceReportResponse
import onku.backend.domain.absence.facade.AbsenceFacade
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/absence/manage")
@Tag(name = "[관리자용] 불참사유서 관련 API")
class AbsenceManagerController(
    private val absenceFacade : AbsenceFacade
) {
    @GetMapping("/{sessionId}")
    @Operation(summary = "세션 별 불참 사유서 제출내역 조회", description = "세션 별로 학회원들이 낸 불참사유서 내역을 조회합니다.")
    fun getMemberAbsenceReports(@PathVariable(name = "sessionId") sessionId: Long)
            : ResponseEntity<SuccessResponse<List<GetMemberAbsenceReportResponse>>> {
        return ResponseEntity.ok(SuccessResponse.ok(absenceFacade.getMemberAbsenceReport(sessionId)))
    }

    @PatchMapping("/{absenceReportId}")
    @Operation(summary = "불참 사유서 벌점 매기기", description = "제출한 불참 사유서에 대해서 벌점을 매긴다.")
    fun estimateAbsenceReport(@PathVariable(name = "absenceReportId") absenceReportId : Long,
                              @RequestBody estimateAbsenceReportRequest: EstimateAbsenceReportRequest)
        : ResponseEntity<SuccessResponse<Boolean>> {
        return ResponseEntity.ok(SuccessResponse.ok(absenceFacade.estimateAbsenceReport(absenceReportId, estimateAbsenceReportRequest)))
    }
}