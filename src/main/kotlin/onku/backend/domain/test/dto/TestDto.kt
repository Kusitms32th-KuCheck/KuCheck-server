package onku.backend.domain.test.dto

import jakarta.validation.constraints.*

data class TestDto(
    @field:NotBlank(message = "title은 공백일 수 없습니다")
    val title: String?,

    @field:Min(value = 1, message = "count는 1 이상이어야 합니다")
    val count: Int?
)
