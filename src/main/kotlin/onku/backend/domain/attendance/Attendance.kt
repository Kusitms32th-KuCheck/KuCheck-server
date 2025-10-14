package onku.backend.domain.attendance

import jakarta.persistence.*
import onku.backend.domain.attendance.enums.AttendanceStatus
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(
    name = "attendance",
    uniqueConstraints = [UniqueConstraint(
        name = "uk_attendance_session_member",
        columnNames = ["session_id", "member_id"]
    )]
)
class Attendance(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    val id: Long? = null,

    @Column(name = "session_id", nullable = false)
    val sessionId: Long,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "attendance_time", nullable = false)
    var attendanceTime: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    var status: AttendanceStatus
) : BaseEntity()
