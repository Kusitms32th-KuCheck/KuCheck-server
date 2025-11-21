package onku.backend.domain.session.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.session.dto.response.*
import onku.backend.domain.session.facade.SessionFacade
import onku.backend.global.response.SuccessResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
    ) : ResponseEntity<SuccessResponse<List<SessionAboutAbsenceResponse>>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.showSessionAboutAbsence()))
    }

    @GetMapping("/this-week")
    @Operation(
        summary = "금주 세션 정보 조회",
        description = "금주 세션 정보를 조회합니다."
    )
    fun showThisWeekSessionInfo() : ResponseEntity<SuccessResponse<List<ThisWeekSessionInfo>>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.showThisWeekSessionInfo()))
    }

    @GetMapping("")
    @Operation(
        summary = "전체 세션 정보 조회",
        description = "전체 세션 정보를 시간순으로 조회합니다."
    )
    fun showAllSessionCards() : ResponseEntity<SuccessResponse<List<SessionCardInfo>>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.showAllSessionCards()))
    }

    @GetMapping("/notice/{sessionId}")
    @Operation(
        summary = "세션 공지 조회",
        description = "세션 Id에 맞는 세션 공지를 조회합니다."
    )
    fun getSessionNotice(
        @PathVariable(name = "sessionId") sessionId : Long
    ) : ResponseEntity<SuccessResponse<GetSessionNoticeResponse>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.getSessionNotice(sessionId)))
    }

    @PatchMapping("/{sessionId}/time")
    @Operation(
        summary = "출석체크가 가능하도록 세션 시간 수정 [TEMP]",
        description = """
            1. Try it out 버튼 클릭
            2. (금주 세션 id를 모른다면 GET /api/v1/session/this-week 에서 sessionId 확인!)
            3. 수정하고자 하는 세션 id 입력
            4. Execute 버튼 눌러서 요청 보내기
            5. 완료! 현재 시각 기준 20분 이후까지 출석이 가능하도록 세션 시간 및 기타 세션 정보들이 설정됨
        """
    )
    fun resetSessionTime(
        @PathVariable sessionId: Long
    ): ResponseEntity<SuccessResponse<SessionTimeResetResponse>> {
        val body = sessionFacade.resetSessionTime(sessionId)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}