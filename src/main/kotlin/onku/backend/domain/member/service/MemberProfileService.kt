package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.dto.MemberProfileResponse
import onku.backend.domain.member.dto.OnboardingRequest
import onku.backend.domain.member.dto.OnboardingResponse
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberProfileService(
    private val memberProfileRepository: MemberProfileRepository,
    private val memberRepository: MemberRepository,
    private val memberService: MemberService
) {
    fun submitOnboarding(member: Member, req: OnboardingRequest): OnboardingResponse {
        if (member.hasInfo) { // 이미 온보딩 완료된 사용자 차단
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        if (member.approval != ApprovalStatus.PENDING) { // PENDING 상태가 아닌 사용자 차단
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        createOrUpdateProfile(member.id!!, req)
        memberService.markOnboarded(member)

        return OnboardingResponse(
            status = ApprovalStatus.PENDING
        )
    }

    private fun createOrUpdateProfile(memberId: Long, req: OnboardingRequest) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val existing = memberProfileRepository.findById(memberId).orElse(null)
        if (existing == null) {
            val profile = MemberProfile(
                member = member,
                name = req.name,
                school = req.school,
                major = req.major,
                part = req.part,
                phoneNumber = req.phoneNumber
            )
            memberProfileRepository.save(profile)
        } else {
            existing.apply(
                name = req.name,
                school = req.school,
                major = req.major,
                part = req.part,
                phoneNumber = req.phoneNumber
            )
        }
    }
    @Transactional(readOnly = true)
    fun getProfileSummary(member: Member): MemberProfileResponse {
        val profile = memberProfileRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }
        return MemberProfileResponse(
            name = profile.name,
            part = profile.part
        )
    }
}
