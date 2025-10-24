package onku.backend.domain.member.repository

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.SocialType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
    fun findBySocialIdAndSocialType(socialId: Long, socialType: SocialType): Member?
    @Query("""
      select m.id from Member m
      where m.hasInfo = true
        and m.approval = onku.backend.domain.member.enums.ApprovalStatus.APPROVED
    """)
    fun findApprovedMemberIds(): List<Long>
}