package onku.backend.domain.member.repository

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberAlarmHistory
import org.springframework.data.jpa.repository.JpaRepository

interface MemberAlarmHistoryRepository : JpaRepository<MemberAlarmHistory, Long> {

    fun findByMemberOrderByCreatedAtDesc(member: Member): List<MemberAlarmHistory>
}
