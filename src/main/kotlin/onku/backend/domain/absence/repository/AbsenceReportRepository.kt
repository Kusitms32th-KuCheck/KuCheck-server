package onku.backend.domain.absence.repository

import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.repository.projection.GetMyAbsenceReportView
import onku.backend.domain.member.Member
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface AbsenceReportRepository : JpaRepository<AbsenceReport, Long> {
    @Query("""
        select
            ar.id as absenceReportId,
            ar.status as absenceType,
            ar.approval as absenceReportApproval,
            ar.createdAt as submitDateTime,
            s.title as sessionTitle,
            s.startTime as sessionStartDateTime
        from AbsenceReport ar
        join ar.session s
        where ar.member = :member
    """)
    fun findMyAbsenceReports(
        @Param("member") member: Member,
        pageable: Pageable
    ): Page<GetMyAbsenceReportView>
}