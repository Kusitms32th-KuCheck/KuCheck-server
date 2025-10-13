package onku.backend.domain.kupick

import jakarta.persistence.*
import onku.backend.domain.member.Member
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(name = "kupick")
class Kupick(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kupick_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member : Member,

    @Column(name = "submit_date")
    var submitDate: LocalDateTime? = null,

    @Column(name = "application_image_url")
    var applicationImageUrl: String,

    @Column(name = "application_date")
    var applicationDate : LocalDateTime? = null,

    @Column(name = "view_image_url")
    var viewImageUrl : String? = null,

    @Column(name = "view_date")
    var viewDate : LocalDateTime? = null,

    @Column(name = "approval")
    var approval : Boolean = false

    ) : BaseEntity() {
    companion object {
        fun createApplication(
            member: Member,
            applicationImageUrl: String,
            applicationDate: LocalDateTime?
        ): Kupick {
            return Kupick(
                member = member,
                applicationImageUrl = applicationImageUrl,
                applicationDate = applicationDate,
                submitDate = applicationDate
            )
        }
    }

    fun submitView(viewImageUrl: String, nowDate: LocalDateTime) {
        this.viewImageUrl = viewImageUrl
        this.viewDate = nowDate
        this.submitDate = nowDate
    }

    fun updateApplication(newUrl: String, newDate: LocalDateTime) {
        this.applicationImageUrl = newUrl
        this.applicationDate = newDate
        this.submitDate = newDate
    }
}