package onku.backend.domain.session

import jakarta.persistence.*
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.enums.SessionCategory
import onku.backend.global.entity.BaseEntity
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "session")
class Session(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    val id: Long? = null,

    @OneToOne(
        fetch = FetchType.LAZY,
        cascade = [CascadeType.PERSIST, CascadeType.MERGE],
        optional = true,
        orphanRemoval = true
    )
    @JoinColumn(name = "session_detail_id")
    var sessionDetail: SessionDetail? = null,

    @Column(name = "title", nullable = false, length = 255)
    var title: String,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    var category: SessionCategory,

    @Column(name = "week", nullable = false, unique = true)
    var week: Long,

    var attendanceFinalized: Boolean = false,
    var attendanceFinalizedAt: LocalDateTime? = null,

    @Column(name = "is_holiday")
    var isHoliday : Boolean = false
    ) : BaseEntity(){
        fun update(sessionSaveRequest: SessionSaveRequest) {
            this.title = sessionSaveRequest.title
            this.week = sessionSaveRequest.week
            this.startDate = sessionSaveRequest.sessionDate
            this.category = sessionSaveRequest.category
            this.isHoliday = sessionSaveRequest.isHoliday
        }
    }
