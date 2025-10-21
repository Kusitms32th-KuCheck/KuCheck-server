package onku.backend.domain.session.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.session.dto.SessionSaveRequest
import onku.backend.domain.session.facade.SessionFacade
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/session/manager")
@Tag(name = "[관리자용] 세션 API", description = "세션 관련 API")
class SessionManagerController(
    private val sessionFacade: SessionFacade
) {
    @PostMapping("")
    @Operation(
        summary = "세션 일정 저장",
        description = "세션 일정들을 저장합니다."
    )
    fun sessionSave(
        @RequestBody @Valid sessionSaveRequestList : List<SessionSaveRequest>
    ) : ResponseEntity<SuccessResponse<Boolean>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.sessionSave(sessionSaveRequestList)))
    }
}