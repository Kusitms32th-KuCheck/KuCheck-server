package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.exception.CustomException
import onku.backend.global.response.SuccessResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import java.time.Duration
import java.util.UUID

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    @Value("\${jwt.onboarding-ttl}") private val onboardingTtl: Duration,
) {
    fun getByEmail(email: String): Member =
        memberRepository.findByEmail(email)
            ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

    @Transactional
    fun register(req: MemberRegisterRequest): ResponseEntity<SuccessResponse<MemberResponse>> {
        val email = req.email.trim()

        if (memberRepository.findByEmail(email) != null) {
            throw CustomException(MemberErrorCode.DUPLICATE_EMAIL)
        }

        val encodedPw = passwordEncoder.encode(req.password)

        val member = Member(
            email = email,
            password = encodedPw,
            socialType = SocialType.EMAIL,
            socialId = UUID.randomUUID().toString(),
        )

        memberRepository.save(member)

        val onboarding = jwtUtil.createOnboardingToken(email, onboardingTtl.toMinutes())
        val headers = HttpHeaders().apply {
            add(HttpHeaders.AUTHORIZATION, "Bearer $onboarding")
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .headers(headers)
            .body(SuccessResponse.ok(MemberResponse.of(member)))
    }

    @Transactional
    fun upsertSocialMember(email: String?, socialId: String, type: SocialType): Member {
        // social로 먼저 조회: Apple 재로그인 시 email 누락 때문
        val bySocial = memberRepository.findBySocialIdAndSocialType(socialId, type)
        if (bySocial != null) {
            bySocial.updateEmail(email)
            return bySocial
        }

        // email이 있으면 email로 조회: email 중복 삽입 방지
        val byEmail = email?.let { memberRepository.findByEmail(it) }
        if (byEmail != null) {
            return byEmail
        }

        // 신규 생성: email 없으면 생성 불가 처리
        val safeEmail = email ?: throw CustomException(AuthErrorCode.OAUTH_EMAIL_SCOPE_REQUIRED)
        val newMember = Member(
            email = safeEmail,
            socialType = type,
            socialId = socialId
        )
        return memberRepository.save(newMember)
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
    fun updateApprovals(items: List<UpdateApprovalRequest>): List<MemberApprovalResponse> {
        if (items.isEmpty()) return emptyList()

        val ids = items.mapNotNull { it.memberId }.toSet()
        val members = memberRepository.findByIdIn(ids)
        if (members.size != ids.size) {
            throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
        }

        val memberMap = members.associateBy { it.id!! }
        val responses = mutableListOf<MemberApprovalResponse>()

        items.forEach { item ->
            val memberId = item.memberId ?: throw CustomException(MemberErrorCode.INVALID_REQUEST)
            val targetStatus = item.status ?: throw CustomException(MemberErrorCode.INVALID_REQUEST)

            if (targetStatus == ApprovalStatus.PENDING) {
                throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
            }

            val member = memberMap[memberId]
                ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

            if (member.approval != ApprovalStatus.PENDING) {
                throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
            }
            when (targetStatus) {
                ApprovalStatus.APPROVED -> member.approve()
                ApprovalStatus.REJECTED -> member.reject()
                ApprovalStatus.PENDING -> { }
            }
            responses.add(
                MemberApprovalResponse(
                    memberId = memberId,
                    role = member.role,
                    approval = member.approval
                )
            )
        }
        memberRepository.saveAll(memberMap.values)
        return responses
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