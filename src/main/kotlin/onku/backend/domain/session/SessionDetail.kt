package onku.backend.domain.session

import jakarta.persistence.*
import onku.backend.global.entity.BaseEntity
import java.time.LocalTime

@Entity
@Table(name = "session_detail")
class SessionDetail(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_detail_id")
    val id: Long? = null,

    @Column(name = "place")
    val place : String,

    @Column(name = "start_time")
    val startTime : LocalTime,

    @Column(name = "end_time")
    val endTime : LocalTime,

    @Column(name = "content")
    val content : String
) : BaseEntity() {
}