package onku.backend.domain.notice

import jakarta.persistence.*

@Entity
@Table(name = "notice_image")
class NoticeImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_image_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    val notice: Notice,

    @Column(name = "image_url", nullable = false, length = 1024)
    val imageUrl: String,

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0
)
