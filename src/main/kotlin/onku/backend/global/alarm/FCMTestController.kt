package onku.backend.global.alarm

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.global.response.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/test/push")
@Tag(name = "푸시 알림 테스트용 API")
class FCMTestController(
    private val fcmService: FCMService
) {
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("")
    @Operation(summary = "푸시 알림 테스트", description = "현재 프론트랑 연동이 안되어서 로그만 확인가능")
    fun pushTest(
        token : String
    ): SuccessResponse<Boolean> {
        fcmService.sendMessageTo(token, "알림 제목", "알림 내용", "알림 눌렀을 때 연결되는 링크")
        return SuccessResponse.ok(true)
    }
}