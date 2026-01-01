package onku.backend.global.response

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.global.response.result.ResponseState

class SuccessResponse<T>(
    @Schema(description = "상태 코드", example = "1")
    val code: Int,

    @Schema(description = "응답 메세지", example = "성공하였습니다.")
    val message: String,

    @Schema(description = "응답 데이터")
    val result: T
) {
    @Schema(description = "성공 여부", example = "true")
    val isSuccess: Boolean = true

    companion object {
        fun <T> of(code: Int, message: String, data: T): SuccessResponse<T> =
            SuccessResponse(code = code, message = message, result = data)

        fun <T> ok(data: T): SuccessResponse<T> =
            of(ResponseState.SUCCESS.code, ResponseState.SUCCESS.message, data)
    }
}
