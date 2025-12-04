package onku.backend.domain.kupick.dto.request

import io.swagger.v3.oas.annotations.media.Schema

data class KupickApprovalRequest(
    @Schema(description = "큐픽 ID", example = "1")
    val kupickId : Long,
    @Schema(description = "승인여부 설정", example = "true / false")
    val approval : Boolean
)
