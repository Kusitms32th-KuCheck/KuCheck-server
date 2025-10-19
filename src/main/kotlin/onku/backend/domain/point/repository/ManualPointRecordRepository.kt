package onku.backend.domain.point.repository

import onku.backend.domain.point.ManualPoint
import org.springframework.data.jpa.repository.JpaRepository

interface ManualPointRecordRepository : JpaRepository<ManualPoint, Long> {
    fun findByMemberIdIn(memberIds: Collection<Long>): List<ManualPoint>
    fun findByMemberId(memberId: Long): ManualPoint?
}
