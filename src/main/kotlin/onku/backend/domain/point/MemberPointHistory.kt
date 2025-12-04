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
    var type: String,

    @Column(name = "points", nullable = false)
    var points: Int,

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: LocalDateTime,

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
                AttendancePointType.ABSENT_WITH_DOC,
                AttendancePointType.ABSENT_WITH_CAUSE -> MemberPointHistory(
                        member = member,
                        category = PointCategory.ATTENDANCE,
                        type = status.name,
                        points = status.points,
                        occurredAt = occurredAt,
                        week = week
                )
            }
        }

        fun ofAttendanceUpdate(
            member: Member,
            status: AttendancePointType,
            occurredAt: LocalDateTime,
            week: Long?,
            diffPoint: Int, // AttendancePointType에 지정된 points가 아닌, 기존 status의 point와 새로이 바뀐 status의 point의 차이를 계산해서 Int 형태로 넣는 diff!
            time: LocalTime? = null
        ): MemberPointHistory {
            return when (status) {
                AttendancePointType.EARLY_LEAVE -> MemberPointHistory(
                    member = member,
                    category = PointCategory.ATTENDANCE,
                    type = status.name,
                    points = diffPoint,
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
                    points = diffPoint,
                    occurredAt = occurredAt,
                    week = week,
                    attendanceTime = time
                )
                AttendancePointType.EXCUSED,
                AttendancePointType.ABSENT,
                AttendancePointType.ABSENT_WITH_DOC,
                AttendancePointType.ABSENT_WITH_CAUSE -> MemberPointHistory(
                    member = member,
                    category = PointCategory.ATTENDANCE,
                    type = status.name,
                    points = diffPoint,
                    occurredAt = occurredAt,
                    week = week
                )
            }
        }

        fun ofManual(
            member: Member,
            manualType: ManualPointType,
            occurredAt: LocalDateTime,
            points: Int
        ): MemberPointHistory {
            return MemberPointHistory(
                member = member,
                category = PointCategory.MANUAL,
                type = manualType.name,
                points = points,
                occurredAt = occurredAt
            )
        }
    }

    fun updateAttendancePointType(
        status: AttendancePointType,
        occurredAt: LocalDateTime,
    ) {
        this.type = status.name
        this.points = status.points
        this.occurredAt = occurredAt
    }
}
