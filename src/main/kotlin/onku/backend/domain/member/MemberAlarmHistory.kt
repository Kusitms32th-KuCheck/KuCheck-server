package onku.backend.domain.member

import jakarta.persistence.*
import onku.backend.global.alarm.enums.AlarmEmojiType
import onku.backend.global.entity.BaseEntity
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
    val type: AlarmEmojiType,
) : BaseEntity()
