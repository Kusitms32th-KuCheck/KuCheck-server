package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MemberService(
    private val memberRepository: MemberRepository,
) {
    fun getByEmail(email: String): Member =
        memberRepository.findByEmail(email)
            ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

    fun getBySocialIdOrNull(socialId: String, socialType: SocialType): Member? =
        memberRepository.findBySocialIdAndSocialType(socialId, socialType)

    fun getBySocialId(socialId: String, socialType: SocialType): Member =
        getBySocialIdOrNull(socialId, socialType)
            ?: throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)

    @Transactional
    fun upsertSocialMember(email: String?, socialId: String, type: SocialType): Member {
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
}
