package onku.backend.domain.session.dto.request

import jakarta.validation.constraints.NotNull

data class UploadSessionImageRequest (
    @field:NotNull val sessionDetailId : Long,
    val imageFileName : String
)