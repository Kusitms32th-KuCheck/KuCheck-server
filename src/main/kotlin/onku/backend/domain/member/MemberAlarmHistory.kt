package onku.backend.domain.member

import onku.backend.domain.member.enums.MemberAlarmType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "member_alarm_history")
class MemberAlarmHistory(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_alarm_history_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "message")
    val message: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: MemberAlarmType,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
