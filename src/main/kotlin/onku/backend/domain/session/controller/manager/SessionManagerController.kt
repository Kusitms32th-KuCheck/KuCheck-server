package onku.backend.domain.session.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.dto.response.GetInitialSessionResponse
import onku.backend.domain.session.dto.response.UpsertSessionDetailResponse
import onku.backend.domain.session.facade.SessionFacade
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @GetMapping("")
    @Operation(
        summary = "초기에 저장한 세션정보 불러오기",
        description = "초기에 저장한 세션정보들을 불러옵니다."
    )
    fun getInitialSession(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) : ResponseEntity<SuccessResponse<PageResponse<GetInitialSessionResponse>>> {
        val safePage = if (page < 1) 0 else page - 1
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.getInitialSession(safePage, size)))
    }

    @PostMapping("/detail")
    @Operation(
        summary = "세션 상세정보 upsert",
        description = "세션 상세정보를 새로 입력하거나 수정합니다."
    )
    fun upsertSessionDetail(
        @RequestBody @Valid upsertSessionDetailRequest : UpsertSessionDetailRequest
    ) : ResponseEntity<SuccessResponse<UpsertSessionDetailResponse>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.upsertSessionDetail(upsertSessionDetailRequest)))
    }


}
