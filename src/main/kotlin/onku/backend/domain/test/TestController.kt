package onku.backend.domain.test

import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "health")
    fun health(): String {
        return "OK"
    }
}