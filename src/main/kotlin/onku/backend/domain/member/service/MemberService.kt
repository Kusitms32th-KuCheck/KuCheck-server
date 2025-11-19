package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.global.exception.CustomException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
) {
    fun getByEmail(email: String): Member =
        memberRepository.findByEmail(email)
            ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

    @Transactional
    fun upsertSocialMember(email: String?, socialId: Long, type: SocialType): Member {
        val existing = memberRepository.findBySocialIdAndSocialType(socialId, type)
        if (existing != null) {
            if (!email.isNullOrBlank() && existing.email != email) {
                existing.updateEmail(email)
            }
            return existing
        }

        val created = Member(
            email = email,
            role = Role.USER,
            socialType = type,
            socialId = socialId,
            hasInfo = false,
            approval = ApprovalStatus.PENDING
        )
        return memberRepository.save(created)
    }

    @Transactional
    fun markOnboarded(member: Member) {
        val m = memberRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (!m.hasInfo) {
            m.onboarded()
            memberRepository.save(m)
        }
    }

    @Transactional
    fun deleteMemberById(memberId: Long) {
        if (!memberRepository.existsById(memberId)) {
            throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
        }
        if (memberProfileRepository.existsByMember_Id(memberId)) {
            memberProfileRepository.deleteByMemberId(memberId)
        }
        memberRepository.deleteById(memberId)
    }

    @Transactional
    fun updateApproval(memberId: Long, targetStatus: ApprovalStatus): MemberApprovalResponse {
        if (targetStatus == ApprovalStatus.PENDING) {
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        val member: Member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (member.approval != ApprovalStatus.PENDING) {
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        when (targetStatus) {
            ApprovalStatus.APPROVED -> member.approve()
            ApprovalStatus.REJECTED -> member.reject()
            ApprovalStatus.PENDING -> {}
        }

        val saved = memberRepository.save(member)

        return MemberApprovalResponse(
            memberId = saved.id!!,
            role = saved.role,
            approval = saved.approval
        )
    }

    @Transactional
    fun updateRole(
        memberId: Long,
        req: UpdateRoleRequest
    ): MemberRoleResponse {
        val newRole = req.role ?: throw CustomException(MemberErrorCode.INVALID_REQUEST)

        val target = memberRepository.findByIdOrNull(memberId)
            ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

        target.role = newRole
        memberRepository.save(target)

        return MemberRoleResponse(
            memberId = target.id!!,
            role = target.role
        )
    }

    @Transactional
    fun updateStaffMembers(req: StaffUpdateRequest): StaffUpdateResponse {
        val targetIds = req.staffMemberIds.toSet()

        // 현재 운영진
        val currentStaffMembers = memberRepository.findByIsStaffTrue()
        val currentStaffIds = currentStaffMembers.mapNotNull { it.id }.toSet()

        val addedStaffIds = (targetIds - currentStaffIds)
        val removedStaffIds = (currentStaffIds - targetIds)

        // 운영진 추가
        if (addedStaffIds.isNotEmpty()) {
            val toAdd = memberRepository.findByIdIn(addedStaffIds)
            if (toAdd.size != addedStaffIds.size) {
                throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
            }
            toAdd.forEach { m ->
                m.isStaff = true
                m.role = Role.STAFF          // isStaff = false → true : role.STAFF
            }
        }

        // 운영진 삭제
        if (removedStaffIds.isNotEmpty()) {
            val toRemove = currentStaffMembers.filter { it.id in removedStaffIds }
            toRemove.forEach { m ->
                m.isStaff = false
                m.role = Role.USER          // isStaff = true → false : role.USER
            }
        }

        return StaffUpdateResponse(
            addedStaffs = addedStaffIds.sorted(),
            removedStaffs = removedStaffIds.sorted()
        )
    }

    @Transactional
    fun updateRoles(req: BulkRoleUpdateRequest): List<MemberRoleResponse> {
        if (req.items.isEmpty()) return emptyList()

        val ids = req.items.mapNotNull { it.memberId }.toSet()
        val members = memberRepository.findByIdIn(ids)
        if (members.size != ids.size) {
            throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
        }

        val memberMap = members.associateBy { it.id!! }
        val responses = mutableListOf<MemberRoleResponse>()

        req.items.forEach { item ->
            val memberId = item.memberId ?: throw CustomException(MemberErrorCode.INVALID_REQUEST)
            val newRole = item.role ?: throw CustomException(MemberErrorCode.INVALID_REQUEST)

            if (newRole == Role.USER || newRole == Role.GUEST) {
                throw CustomException(MemberErrorCode.INVALID_REQUEST)
            }

            val member = memberMap[memberId]
                ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

            member.role = newRole

            responses.add(
                MemberRoleResponse(
                    memberId = memberId,
                    role = member.role
                )
            )
        }
        memberRepository.saveAll(memberMap.values)
        return responses
    }
}