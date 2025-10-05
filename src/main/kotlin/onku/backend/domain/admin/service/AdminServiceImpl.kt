package onku.backend.domain.admin.service

import onku.backend.domain.admin.dto.MemberApprovalResponse
import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AdminServiceImpl(
    private val memberRepository: MemberRepository
) : AdminService {

    override fun updateApproval(memberId: Long, targetStatus: ApprovalStatus): MemberApprovalResponse {
        val member: Member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (member.approval != ApprovalStatus.PENDING) {
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        when (targetStatus) {
            ApprovalStatus.APPROVED -> member.approve()
            ApprovalStatus.REJECTED -> member.reject()
            ApprovalStatus.PENDING -> throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        val saved = memberRepository.save(member)

        return MemberApprovalResponse(
            memberId = saved.id!!,
            role = saved.role,
            approval = saved.approval
        )
    }
}
