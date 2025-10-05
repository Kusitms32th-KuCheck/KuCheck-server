package onku.backend.domain.admin.service

import onku.backend.domain.admin.dto.MemberApprovalResponse
import onku.backend.domain.member.enums.ApprovalStatus

interface AdminService {
    fun updateApproval(memberId: Long, targetStatus: ApprovalStatus): MemberApprovalResponse
}