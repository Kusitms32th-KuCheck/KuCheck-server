package onku.backend.domain.kupick.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.kupick.dto.request.KupickApprovalRequest
import onku.backend.domain.kupick.dto.response.ShowUpdateResponseDto
import onku.backend.domain.kupick.facade.KupickFacade
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/kupick/manage")
@Tag(name = "큐픽 API", description = "큐픽 관련 CRUD API")
class KupickManagerController(
    private val kupickFacade: KupickFacade
) {
    @GetMapping("/update")
    @Operation(
        summary = "큐픽 신청 현황",
        description = "학회원들의 큐픽 서류 신청 현황 조회"
    )
    fun submitApplication(
        year : Int,
        month : Int
    ) : ResponseEntity<SuccessResponse<List<ShowUpdateResponseDto>>> {
        return ResponseEntity.ok(SuccessResponse.ok(kupickFacade.showUpdate(year, month)))
    }

    @PostMapping("/approval")
    @Operation(
        summary = "큐픽 승인/미승인 처리",
        description = "학회원들 큐픽 신청 승인 및 미승인 처리"
    )
    fun decideKupickApproval(
        kupickApprovalRequest: KupickApprovalRequest
    ) : ResponseEntity<SuccessResponse<Boolean>> {
        return ResponseEntity.ok(SuccessResponse.ok(kupickFacade.decideApproval(kupickApprovalRequest)))
    }
}