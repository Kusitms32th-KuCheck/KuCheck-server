package onku.backend.domain.session.repository

import onku.backend.domain.session.Session
import onku.backend.domain.session.enums.SessionCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface SessionRepository : CrudRepository<Session, Long> {

    @Query(
        value = """
        SELECT *
        FROM session s
        WHERE :now BETWEEN TIMESTAMPADD(SECOND, -s.open_grace_seconds, s.start_time)
                        AND TIMESTAMPADD(SECOND,  s.close_grace_seconds, s.end_time)
        ORDER BY s.start_time DESC
        LIMIT 1
        """,
        nativeQuery = true
    )
    fun findOpenSession(@Param("now") now: LocalDateTime): Session?

    @Query("""
        SELECT s
        FROM Session s
        WHERE s.startTime >= :now AND s.category <> :restCategory
    """)
    fun findUpcomingSessions(
        @Param("now") now: LocalDateTime,
        pageable: Pageable,
        @Param("restCategory") restCategory: SessionCategory = SessionCategory.REST
    ): Page<Session>

    @Query("""
        select s.startTime 
        from Session s 
        where s.startTime >= :start and s.startTime < :end
    """)
    fun findStartTimesBetween(
        @Param("start") start: LocalDateTime,
        @Param("end") end: LocalDateTime
    ): List<LocalDateTime>
}
