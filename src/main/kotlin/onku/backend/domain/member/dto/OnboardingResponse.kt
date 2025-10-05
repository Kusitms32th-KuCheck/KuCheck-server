package onku.backend.domain.member.dto

import onku.backend.domain.member.enums.ApprovalStatus

data class OnboardingResponse(
    val status: ApprovalStatus,
    val message: String
)