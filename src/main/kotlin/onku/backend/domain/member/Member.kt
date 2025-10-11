package onku.backend.domain.member

import jakarta.persistence.*
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType

@Entity
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long? = null,

    @Column(nullable = false, unique = true, length = 255)
    var email: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: Role = Role.USER,

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    val socialType: SocialType,

    @Column(name = "social_id", nullable = false, length = 100)
    val socialId: String,

    @Column(name = "has_info", nullable = false)
    var hasInfo: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "approval", nullable = false, length = 20)
    var approval: ApprovalStatus = ApprovalStatus.PENDING
) {
    fun approve() { this.approval = ApprovalStatus.APPROVED }
    fun reject() { this.approval = ApprovalStatus.REJECTED }
    fun onboarded() { this.hasInfo = true }
    fun updateEmail(newEmail: String?) {
        if (newEmail.isNullOrBlank()) return
        if (this.email != newEmail) this.email = newEmail
    }
}
