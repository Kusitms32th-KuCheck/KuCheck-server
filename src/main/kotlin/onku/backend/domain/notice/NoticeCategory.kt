package onku.backend.domain.notice

import jakarta.persistence.*
import onku.backend.domain.notice.enums.NoticeCategoryColor

@Entity
@Table(
    name = "category",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_category_name", columnNames = ["name"]),
        UniqueConstraint(name = "uq_category_color", columnNames = ["color"])
    ]
)
class NoticeCategory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    val id: Long? = null,

    @Column(name = "name", nullable = false, length = 50)
    var name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "color", nullable = false)
    var color: NoticeCategoryColor
) {
    @ManyToMany(mappedBy = "categories")
    val notices: MutableSet<Notice> = linkedSetOf()
}