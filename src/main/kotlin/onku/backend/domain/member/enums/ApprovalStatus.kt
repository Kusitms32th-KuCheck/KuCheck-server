package onku.backend.domain.member.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
    description = "회원가입 승인 상태",
    example = "APPROVED"
)
enum class ApprovalStatus {
    @Schema(description = "승인 대기") PENDING,
    @Schema(description = "승인 완료") APPROVED,
    @Schema(description = "반려") REJECTED
}