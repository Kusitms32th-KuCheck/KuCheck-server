package onku.backend.domain.point.repository

import onku.backend.domain.member.Member
import onku.backend.domain.point.MemberPoint
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MemberPointRepository : JpaRepository<MemberPoint, Long> {

    fun findByMemberOrderByOccurredAtDesc(member: Member, pageable: Pageable): Page<MemberPoint>

    interface MemberPointSums {
        fun getPlusPoints(): Long
        fun getMinusPoints(): Long
        fun getTotalPoints(): Long
    }

    @Query(
        """
        SELECT COALESCE(SUM(CASE WHEN r.points > 0 THEN r.points ELSE 0 END), 0) AS plusPoints,
               COALESCE(SUM(CASE WHEN r.points < 0 THEN r.points ELSE 0 END), 0) AS minusPoints,
               COALESCE(SUM(r.points), 0) AS totalPoints
        FROM MemberPoint r
        WHERE r.member = :member
        """
    )
    fun sumPointsForMember(member: Member): MemberPointSums
}
