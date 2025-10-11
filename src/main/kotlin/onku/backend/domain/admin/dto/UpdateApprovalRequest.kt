package onku.backend.domain.admin.dto

import jakarta.validation.constraints.NotNull
import onku.backend.domain.member.enums.ApprovalStatus

data class UpdateApprovalRequest(
    @field:NotNull
    val status: ApprovalStatus
)