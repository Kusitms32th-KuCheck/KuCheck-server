package onku.backend.domain.member

import jakarta.persistence.*
import onku.backend.domain.member.enums.Part

@Entity
class MemberProfile(
    @Id
    @Column(name = "member_id")
    var memberId: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "member_id")
    val member: Member,

    @Column(length = 100)
    var name: String? = null,

    @Column(length = 100)
    var school: String? = null,

    @Column(length = 100)
    var major: String? = null,

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    var part: Part,

    @Column(name = "phone_number", length = 30)
    var phoneNumber: String? = null
) {

    fun apply(
        name: String,
        school: String?,
        major: String?,
        part: Part,
        phoneNumber: String?
    ) {
        this.name = name
        this.school = school
        this.major = major
        this.part = part
        this.phoneNumber = phoneNumber
    }
}
