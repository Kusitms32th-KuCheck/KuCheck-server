package onku.backend.domain.absence.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.absence.dto.response.GetMemberAbsenceReportResponse
import onku.backend.domain.absence.facade.AbsenceFacade
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
}