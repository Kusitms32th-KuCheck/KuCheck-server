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
import java.time.LocalTime

interface SessionRepository : CrudRepository<Session, Long> {

    fun findTopByStartDateAndSessionDetail_StartTimeLessThanEqualAndSessionDetail_EndTimeGreaterThanEqualOrderBySessionDetail_StartTimeDesc(
        date: LocalDate,
        time1: LocalTime,
        time2: LocalTime
    ): Session?

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
        select function('timestamp', sess.startDate, d.startTime)
        from Session sess join sess.sessionDetail d
        where function('timestamp', sess.startDate, d.startTime) >= :start
          and function('timestamp', sess.startDate, d.startTime) <  :end
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
