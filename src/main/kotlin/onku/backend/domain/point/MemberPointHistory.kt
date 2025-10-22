package onku.backend.domain.point

import jakarta.persistence.*
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.member.Member
import onku.backend.domain.point.enums.ManualPointType
import onku.backend.domain.point.enums.PointCategory
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(
    indexes = [
        Index(name = "idx_member_point_member_date", columnList = "member_id, occurred_at"),
        Index(name = "idx_member_point_category", columnList = "category"),
        Index(name = "idx_member_point_type", columnList = "type")
    ]
)
class MemberPointHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    val category: PointCategory,

    @Column(name = "type", nullable = false)
    val type: String,

    @Column(name = "points", nullable = false)
    val points: Int,

    @Column(name = "occurred_at", nullable = false)
    val occurredAt: LocalDateTime,

    @Column(name = "week")
    val week: Long? = null,

    @Column(name = "attendance_time")
    val attendanceTime: LocalTime? = null,

    @Column(name = "early_leave_time")
    val earlyLeaveTime: LocalTime? = null
) : BaseEntity() {

    companion object {
        fun ofAttendance(
            member: Member,
            status: AttendancePointType,
            occurredAt: LocalDateTime,
            week: Long,
            time: LocalTime? = null
        ): MemberPointHistory {
            return when (status) {
                AttendancePointType.EARLY_LEAVE -> MemberPointHistory(
                    member = member,
                    category = PointCategory.ATTENDANCE,
                    type = status.name,
                    points = status.points,
                    occurredAt = occurredAt,
                    week = week,
                    earlyLeaveTime = time
                )
                AttendancePointType.PRESENT,
                AttendancePointType.PRESENT_HOLIDAY,
                AttendancePointType.LATE -> MemberPointHistory(
                    member = member,
                    category = PointCategory.ATTENDANCE,
                    type = status.name,
                    points = status.points,
                    occurredAt = occurredAt,
                    week = week,
                    attendanceTime = time
                )
                AttendancePointType.EXCUSED,
                AttendancePointType.ABSENT,
                AttendancePointType.ABSENT_WITH_DOC -> MemberPointHistory(
                    member = member,
                    category = PointCategory.ATTENDANCE,
                    type = status.name,
                    points = status.points,
                    occurredAt = occurredAt,
                    week = week
                )
            }
        }

        fun ofManual(
            member: Member,
            manualType: ManualPointType,
            occurredAt: LocalDateTime
        ): MemberPointHistory {
            return MemberPointHistory(
                member = member,
                category = PointCategory.MANUAL,
                type = manualType.name,
                points = manualType.points,
                occurredAt = occurredAt
            )
        }
    }
}
