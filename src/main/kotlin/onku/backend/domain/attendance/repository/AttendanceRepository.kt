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
        INSERT INTO attendance (session_id, member_id, status, attendance_time)
        VALUES (:sessionId, :memberId, :status, :attendanceTime)
        """,
        nativeQuery = true
    )
    fun insertOnly(
        @Param("sessionId") sessionId: Long,
        @Param("memberId") memberId: Long,
        @Param("status") status: String,
        @Param("attendanceTime") attendanceTime: LocalDateTime
    ): Int
}
