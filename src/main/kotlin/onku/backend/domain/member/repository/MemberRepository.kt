package onku.backend.domain.member.repository

import onku.backend.domain.member.Member
import onku.backend.domain.member.enums.SocialType
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByEmail(email: String): Member?
    fun findBySocialIdAndSocialType(socialId: String, socialType: SocialType): Member?
}