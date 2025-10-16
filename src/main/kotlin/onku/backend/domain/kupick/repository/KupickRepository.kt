package onku.backend.domain.kupick.repository

import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.repository.projection.KupickUrls
import onku.backend.domain.kupick.repository.projection.KupickWithProfile
import onku.backend.domain.member.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface KupickRepository : JpaRepository<Kupick, Long> {
    @Query("""
        SELECT k FROM Kupick k
        WHERE k.member = :member
          AND k.applicationDate >= :start
          AND k.applicationDate <  :end
    """)
    fun findThisMonthByMember(
        member: Member,
        start: LocalDateTime,
        end: LocalDateTime
    ): Kupick?

    fun findFirstByMemberAndApplicationDateBetween(
        member: Member,
        start: LocalDateTime,
        end: LocalDateTime
    ): Kupick?

    @Query("""
    SELECT k.applicationImageUrl AS applicationImageUrl,
           k.viewImageUrl AS viewImageUrl,
           k.applicationDate AS applicationDate,
           k.viewDate AS viewDate
    FROM Kupick k
    WHERE k.member = :member
      AND k.applicationDate >= :start
      AND k.applicationDate <  :end
""")
    fun findUrlsForMemberInMonth(
        @Param("member") member: Member,
        @Param("start") startOfMonth: LocalDateTime,
        @Param("end") startOfNextMonth: LocalDateTime
    ): KupickUrls?

    @Query("""
        select k as kupick, mp as memberProfile
        from Kupick k
        join k.member m
        left join MemberProfile mp on mp.member = m
        where k.submitDate >= :start and k.submitDate < :end
    """)
    fun findAllWithProfile(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime,
        pageable: Pageable
    ): Page<KupickWithProfile>
}