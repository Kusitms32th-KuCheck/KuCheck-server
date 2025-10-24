package onku.backend.domain.session.repository

import onku.backend.domain.session.Session
import onku.backend.domain.session.enums.SessionCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface SessionRepository : CrudRepository<Session, Long> {

    @Query("""
        SELECT s
        FROM Session s
        JOIN s.sessionDetail sd
        WHERE function('timestamp', s.startDate, sd.startTime) <= :startBound
          AND function('timestamp', s.startDate, sd.endTime)   >= :endBound
        ORDER BY s.startDate DESC, sd.startTime DESC
    """)
    fun findOpenWindow(
        @Param("startBound") startBound: LocalDateTime,
        @Param("endBound") endBound: LocalDateTime
    ): List<Session>

    @Query(
        """
        SELECT s
        FROM Session s
        WHERE s.startDate >= :now AND s.category <> :restCategory
    """
    )
    fun findUpcomingSessions(
        @Param("now") now: LocalDate,
        @Param("restCategory") restCategory: SessionCategory = SessionCategory.REST
    ): List<Session>


    @Query("""
        SELECT s
        FROM Session s
        """)
    fun findAll(pageable: Pageable): Page<Session>

    @Query("""
        SELECT function('timestamp', sess.startDate, d.startTime)
        FROM Session sess join sess.sessionDetail d
        WHERE function('timestamp', sess.startDate, d.startTime) >= :start
          AND function('timestamp', sess.startDate, d.startTime) <  :end
    """)
    fun findStartTimesBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<LocalDateTime>

    @Query("""
        SELECT s
        FROM Session s
        JOIN s.sessionDetail sd
        WHERE s.attendanceFinalized = false
          AND function('timestamp', s.startDate, sd.startTime) <= :pivot
    """)
    fun findFinalizeDue(@Param("pivot") pivot: LocalDateTime): List<Session>

    @Query("""
        SELECT s
        FROM Session s
        JOIN s.sessionDetail sd
        WHERE s.attendanceFinalized = false
          AND function('timestamp', s.startDate, sd.startTime) > :pivot
    """)
    fun findUnfinalizedAfter(@Param("pivot") pivot: LocalDateTime): List<Session>
}
