package onku.backend.domain.notice.dto.category

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import onku.backend.domain.notice.enums.NoticeCategoryColor

data class NoticeCategoryUpdateRequest(
    @field:NotBlank(message = "카테고리 이름은 필수입니다.")
    @field:Size(max = 6, message = "카테고리 이름은 7자 이상 등록할 수 없습니다.")
    val name: String,
    val color: NoticeCategoryColor
)
