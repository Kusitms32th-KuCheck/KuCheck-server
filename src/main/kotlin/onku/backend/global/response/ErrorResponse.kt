package onku.backend.global.response

import io.swagger.v3.oas.annotations.media.Schema

class ErrorResponse<T>(
    @Schema(description = "예외 코드", example = "COMMON500")
    val code: String,

    @Schema(description = "예외 메세지", example = "실패하였습니다.")
    val message: String,

    @Schema(description = "예외 참고 데이터")
    val result: T? = null
) {
    @Schema(description = "성공 여부", example = "false")
    val isSuccess: Boolean = false

    companion object {
        fun <T> of(code: String, message: String): ErrorResponse<T> =
            ErrorResponse(code = code, message = message, result = null)

        fun <T> ok(code: String, message: String, data: T?): ErrorResponse<T> =
            ErrorResponse(code = code, message = message, result = data)
    }
}
