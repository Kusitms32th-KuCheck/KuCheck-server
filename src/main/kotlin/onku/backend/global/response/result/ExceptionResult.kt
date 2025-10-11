package onku.backend.global.response.result

import io.swagger.v3.oas.annotations.media.Schema

class ExceptionResult {

    data class ServerErrorData(
        @Schema(description = "오류 발생 클래스", example = "org.example.XX")
        val errorClass: String? = null,

        @Schema(description = "오류 메세지")
        val errorMessage: String? = null
    )

    data class ParameterData(
        @Schema(description = "오류가 발생한 필드", example = "title")
        val key: String? = null,

        @Schema(description = "넣은 요청값", example = "null")
        val value: String? = null,

        @Schema(description = "오류 발생 이유", example = "공백일 수 없습니다")
        val reason: String? = null
    )
}
