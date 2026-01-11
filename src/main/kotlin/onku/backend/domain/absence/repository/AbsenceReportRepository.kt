package onku.backend.domain.absence.repository

import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.repository.projection.GetMyAbsenceReportView
import onku.backend.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AbsenceReportRepository : JpaRepository<AbsenceReport, Long> {
    @Query(
        """
        select
            ar.id as absenceReportId,
            ar.submitType as absenceSubmitType,
            ar.approval as absenceReportApproval,
            ar.createdAt as submitDateTime,
            s.title as sessionTitle,
            s.startDate as sessionStartDateTime
        from AbsenceReport ar
        join ar.session s
        where ar.member = :member
    """
    )
    fun findMyAbsenceReports(
        @Param("member") member: Member
    ): List<GetMyAbsenceReportView>

    @Query("""
        select ar from AbsenceReport ar
        where ar.session.id = :sessionId and ar.member.id in :memberIds
      """)
    fun findReportsBySessionAndMembers(
        @Param("sessionId") sessionId: Long,
        @Param("memberIds") memberIds: Collection<Long>
    ): List<AbsenceReport>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from AbsenceReport ar where ar.session.id = :sessionId")
    fun deleteAllBySessionId(@Param("sessionId") sessionId: Long): Int

    @Query("""
        SELECT a
        FROM AbsenceReport a
        JOIN FETCH a.member m
        JOIN FETCH m.memberProfile mp
        WHERE a.session.id = :sessionId
    """)
    fun findAllBySessionId(@Param("sessionId") sessionId: Long): List<AbsenceReport>
}