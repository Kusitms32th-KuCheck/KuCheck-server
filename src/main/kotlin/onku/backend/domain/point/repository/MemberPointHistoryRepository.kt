package onku.backend.domain.point.repository

import onku.backend.domain.member.Member
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.enums.PointCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface MemberPointHistoryRepository : JpaRepository<MemberPointHistory, Long> {

    fun findByMemberOrderByOccurredAtDesc(member: Member, pageable: Pageable): Page<MemberPointHistory>

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
        FROM MemberPointHistory r
        WHERE r.member = :member
        """
    )
    fun sumPointsForMember(@Param("member") member: Member): MemberPointSums

    // 운영진 조회용 쿼리
    interface MonthlyAttendanceSumRow {
        fun getMemberId(): Long
        fun getMonth(): Int
        fun getPoints(): Long
    }

    @Query(
        """
        SELECT r.member.id AS memberId,
               function('month', r.occurredAt) AS month,
               SUM(r.points)                     AS points
        FROM MemberPointHistory r
        WHERE r.member.id IN :memberIds
          AND r.category = :category
          AND r.occurredAt >= :start AND r.occurredAt < :end
        GROUP BY r.member.id, function('month', r.occurredAt)
        """
    )
    fun sumAttendanceByMemberAndMonth(
        @Param("memberIds") memberIds: Collection<Long>,
        @Param("category") category: PointCategory,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<MonthlyAttendanceSumRow>

    @Query(
        """
        SELECT r
        FROM MemberPointHistory r
        WHERE r.member.id IN :memberIds
          AND r.category = :category
          AND r.occurredAt >= :start AND r.occurredAt < :end
        """
    )
    fun findAttendanceByMemberIdsBetween(
        @Param("memberIds") memberIds: Collection<Long>,
        @Param("category") category: PointCategory,
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<MemberPointHistory>
}