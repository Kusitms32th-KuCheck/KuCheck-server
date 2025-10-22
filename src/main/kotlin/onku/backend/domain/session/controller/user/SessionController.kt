package onku.backend.domain.session.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.session.dto.response.SessionAboutAbsenceResponse
import onku.backend.domain.session.facade.SessionFacade
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/session")
@Tag(name = "세션 API", description = "세션 관련 API")
class SessionController(
    private val sessionFacade: SessionFacade
) {
    @GetMapping("/absence")
    @Operation(
        summary = "불참사유서 제출 페이지에서 세션 정보 조회",
        description = "불참사유서 제출 페이지에서 세션 정보를 조회합니다."
    )
    fun showSessionAboutAbsence(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ) : ResponseEntity<SuccessResponse<PageResponse<SessionAboutAbsenceResponse>>> {
        val safePage = if (page < 1) 0 else page - 1
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.showSessionAboutAbsence(safePage, size)))
    }
}