package onku.backend.domain.session

import jakarta.persistence.*
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "session_detail")
class SessionDetail(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_detail_id")
    val id: Long? = null,

    @Column(name = "place", nullable = false)
    var place: String,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime,

    @Column(name = "content", nullable = false)
    var content: String,
) : BaseEntity() {
}