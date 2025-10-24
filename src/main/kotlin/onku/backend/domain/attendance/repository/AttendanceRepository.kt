package onku.backend.domain.attendance.repository

import onku.backend.domain.attendance.Attendance
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

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
}
