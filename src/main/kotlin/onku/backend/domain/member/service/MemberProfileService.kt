package onku.backend.domain.member.service

import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.dto.OnboardingRequest
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
    fun createOrUpdateProfile(memberId: Long, req: OnboardingRequest) {
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
        memberService.markOnboarded(member)
    }
}
