package onku.backend.domain.notice

import jakarta.persistence.*
import onku.backend.domain.member.Member
import onku.backend.domain.notice.enums.NoticeStatus
import java.time.LocalDateTime

@Entity
@Table(name = "notice")
class Notice(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @Column(name = "title")
    var title: String? = null,

    @Column(name = "content", columnDefinition = "TEXT")
    var content: String? = null,

    @Column(name = "published_at")
    var publishedAt: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: NoticeStatus? = NoticeStatus.PUBLISHED
) {
    @ManyToMany
    @JoinTable(
        name = "notice_category",
        joinColumns = [JoinColumn(name = "notice_id")],
        inverseJoinColumns = [JoinColumn(name = "category_id")]
    )
    var categories: MutableSet<NoticeCategory> = linkedSetOf()

    @OneToMany(mappedBy = "notice", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<NoticeImage> = mutableListOf()
}
