package onku.backend.domain.attendance.repository

import onku.backend.domain.attendance.Attendance
import onku.backend.domain.attendance.enums.AttendancePointType
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.time.LocalDateTime

interface StatusCountProjection {
    fun getStatus(): AttendancePointType
    fun getCnt(): Long
}

interface AttendanceRepository : CrudRepository<Attendance, Long> {

    fun existsBySessionIdAndMemberId(sessionId: Long, memberId: Long): Boolean

    @Modifying
    @Query(
        value = """
        INSERT INTO attendance
            (session_id, member_id, status, attendance_time, created_at, updated_at) 
        VALUES
            (:sessionId, :memberId, :status, :attendanceTime, :createdAt, :updatedAt)
        """,
        nativeQuery = true
    )
    fun insertOnly(
        @Param("sessionId") sessionId: Long,
        @Param("memberId") memberId: Long,
        @Param("status") status: String,
        @Param("attendanceTime") attendanceTime: LocalDateTime,
        @Param("createdAt") createdAt: LocalDateTime,
        @Param("updatedAt") updatedAt: LocalDateTime
    ): Int

    fun findByMemberIdInAndAttendanceTimeBetween(
        memberIds: Collection<Long>,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Attendance>

    @Query("""
      select a.memberId from Attendance a
      where a.sessionId = :sessionId
    """)
    fun findMemberIdsBySessionId(@Param("sessionId") sessionId: Long): List<Long>

    @Query("""
        select a.status as status, count(a) as cnt
        from Attendance a
        where function('date', a.attendanceTime) between :startDate and :endDate
        group by a.status
    """)
    fun countGroupedByStatusBetweenDates(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<StatusCountProjection>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Attendance a where a.sessionId = :sessionId")
    fun deleteAllBySessionId(@Param("sessionId") sessionId: Long): Int
}
