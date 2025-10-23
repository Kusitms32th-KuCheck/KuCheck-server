package onku.backend.domain.session

import jakarta.persistence.*
import onku.backend.domain.session.enums.SessionCategory
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "session")
class Session(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    val id: Long? = null,

    @Column(name = "title", nullable = false, length = 255)
    val title: String,

    @Column(name = "start_time", nullable = false)
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalDateTime,

    @Column(name = "place", nullable = false, length = 255)
    val place: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    val category: SessionCategory,

    @Column(name = "week", nullable = false)
    val week: Long,

    @Column(name = "is_reward", nullable = false)
    val isReward: Boolean,

    var attendanceFinalized: Boolean = false,
    var attendanceFinalizedAt: LocalDateTime? = null,

    @Column(name = "open_grace_seconds", nullable = false) // 세션 시작 이전 N초부터 출석 허용
    val openGraceSeconds: Long = 0,

    @Column(name = "close_grace_seconds", nullable = false) // 세션 종료 이후 N초까지 출석 허용
    val closeGraceSeconds: Long = 0,

    @Column(name = "late_threshold_time", nullable = false) // 지각 기준 시각
    val lateThresholdTime: LocalDateTime
) : BaseEntity()
