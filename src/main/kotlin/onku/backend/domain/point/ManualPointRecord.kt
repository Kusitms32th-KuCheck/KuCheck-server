package onku.backend.domain.point

import jakarta.persistence.*
import onku.backend.domain.member.Member

@Entity
class ManualPointRecord(
    @Id
    @Column(name = "member_id")
    val memberId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(name = "study_points")
    var studyPoints: Int? = 0,

    @Column(name = "kupporters_points")
    var kupportersPoints: Int? = 0,

    @Column(name = "memo", columnDefinition = "TEXT")
    var memo: String? = null
)
