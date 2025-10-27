package onku.backend.domain.session.repository

import onku.backend.domain.session.Session
import onku.backend.domain.session.dto.response.ThisWeekSessionInfo
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

    @Query(
        """
        SELECT s
        FROM Session s
        ORDER BY s.startDate ASC
    """
    )
    fun findAllSessionsOrderByStartDate(): List<Session>


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
          AND (s.startDate < :pivotDate or (s.startDate = :pivotDate and sd.startTime <= :pivotTime))
    """)
    fun findFinalizeDue(
        @Param("pivotDate") pivotDate: LocalDate,
        @Param("pivotTime") pivotTime: LocalTime
    ): List<Session>

    @Query("""
        SELECT s
        FROM Session s
        JOIN s.sessionDetail sd
        WHERE s.attendanceFinalized = false
          AND (s.startDate > :pivotDate or (s.startDate = :pivotDate and sd.startTime > :pivotTime))
    """)
    fun findUnfinalizedAfter(
        @Param("pivotDate") pivotDate: LocalDate,
        @Param("pivotTime") pivotTime: LocalTime
    ): List<Session>


    @Query("""
        SELECT
            s.id as sessionId,
            sd.id as sessionDetailId,
            s.title as title,
            sd.place as place,
            s.startDate as startDate,
            sd.startTime as startTime,
            sd.endTime as endTime
        FROM Session s
        LEFT JOIN s.sessionDetail sd
        WHERE s.startDate BETWEEN :start AND :end
        ORDER BY s.startDate ASC
    """)
    fun findThisWeekSunToSat(
        @Param("start") start: LocalDate,
        @Param("end") end: LocalDate
    ): List<ThisWeekSessionInfo>
}
