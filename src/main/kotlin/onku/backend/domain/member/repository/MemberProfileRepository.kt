package onku.backend.domain.member.repository

import onku.backend.domain.member.MemberProfile
import org.springframework.data.jpa.repository.JpaRepository

interface MemberProfileRepository : JpaRepository<MemberProfile, Long>
