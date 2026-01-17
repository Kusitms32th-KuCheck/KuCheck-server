package onku.backend.global.auth.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class EmailLoginRequest(
    @field:Email(message = "올바른 이메일 형식이어야 합니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    @Schema(example = "test@gmail.com")
    val email: String,

    @field:NotBlank(message = "비밀번호는 필수입니다.")
    @Schema(example = "test1234")
    val password: String
)
