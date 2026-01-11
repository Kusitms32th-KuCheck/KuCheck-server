package onku.backend.domain.session

import jakarta.persistence.*
import onku.backend.global.entity.BaseEntity

@Entity
@Table(name = "session_image")
class SessionImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_image_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_detail_id")
    val sessionDetail: SessionDetail,

    @Column(name = "url")
    val url : String
) : BaseEntity() {

}