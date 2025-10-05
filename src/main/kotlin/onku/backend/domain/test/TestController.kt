package onku.backend.domain.test

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import onku.backend.domain.member.Member
import onku.backend.domain.test.dto.TestDto
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.response.SuccessResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Hidden
@RestController
@RequestMapping("/test")
@Tag(name = "테스트용 API")
class TestController {
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/health")
    @Operation(summary = "헬스 체크", description = "health")
    fun health(): String {
        return "OK"
    }

    @PostMapping("/request-body")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "body 검증 테스트", description = "TestDto의 count의 @Min 조건 검증")
    fun beanValidation(@RequestBody @Valid body: TestDto): SuccessResponse<String> =
        SuccessResponse.ok("VALID")

    @GetMapping("/param")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Param 검증 테스트", description = "@RequestParam의 @Min 조건 검증")
    fun paramValidation(
        @RequestParam @Min(1, message = "age는 1 이상이어야 합니다") age: Int
    ): SuccessResponse<String> =
        SuccessResponse.ok("AGE=$age")

    @PostMapping("/json")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "JSON 문법 에러 검증 테스트", description = "콤마 누락 등의 문법오류와 필드의 타입오류 검증")
    fun jsonGrammar(@RequestBody body: TestDto): SuccessResponse<String> =
        SuccessResponse.ok("PARSED")

    @GetMapping("/custom-error")
    @Operation(summary = "커스텀 예외 테스트")
    fun custom(): String {
        throw CustomException(ErrorCode.INVALID_REQUEST)
    }

    @GetMapping("/untracked-error")
    @Operation(summary = "미등록 예외 테스트")
    fun untracked(): String {
        throw IllegalStateException("테스트용 미등록 예외")
    }

    @GetMapping("/annotation")
    fun annotation(@CurrentMember member: Member): SuccessResponse<String> =
        SuccessResponse.ok("MEMBER_ID=$member.id")
}