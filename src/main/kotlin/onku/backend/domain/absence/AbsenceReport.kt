package onku.backend.domain.absence

import jakarta.persistence.*
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceApprovedType
import onku.backend.domain.absence.enums.AbsenceSubmitType
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import onku.backend.global.entity.BaseEntity
import java.time.LocalDateTime

@Entity
@Table(
    name = "absence_report",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_absence_member_session",
            columnNames = ["member_id", "session_id"]
        )
    ]
)
class AbsenceReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "absence_report_id")
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    var session : Session,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member : Member,

    @Column(name = "url")
    var url : String,

    @Column(name = "submit_type")
    @Enumerated(EnumType.STRING) // 사용자가 제출한 사유서 type
    var submitType : AbsenceSubmitType = AbsenceSubmitType.ABSENT,

    @Column(name = "approved_type")
    @Enumerated(EnumType.STRING) // 운영진이 승인한 사유서 type
    var approvedType: AbsenceApprovedType = AbsenceApprovedType.ABSENT,

    @Column(name = "reason")
    var reason : String,

    @Column(name = "approval")
    @Enumerated(EnumType.STRING)
    val approval : AbsenceReportApproval,

    @Column(name = "lateDateTime")
    var lateDateTime: LocalDateTime?,

    @Column(name = "leaveDateTime")
    var leaveDateTime: LocalDateTime?
) : BaseEntity() {
    companion object {
        fun createAbsenceReport(
            member: Member,
            session : Session,
            submitAbsenceReportRequest: SubmitAbsenceReportRequest,
            fileKey : String,
        ): AbsenceReport {
            return AbsenceReport(
                member = member,
                session = session,
                url = fileKey,
                submitType = submitAbsenceReportRequest.submitType,
                approvedType = AbsenceApprovedType.ABSENT,
                reason = submitAbsenceReportRequest.reason,
                approval = AbsenceReportApproval.SUBMIT,
                leaveDateTime = submitAbsenceReportRequest.leaveDateTime,
                lateDateTime = submitAbsenceReportRequest.lateDateTime
            )
        }
    }

    fun updateAbsenceReport(
        submitAbsenceReportRequest: SubmitAbsenceReportRequest,
        fileKey: String,
        session: Session
    ) {
        this.session = session
        this.reason = submitAbsenceReportRequest.reason
        this.url = fileKey
        this.submitType = submitAbsenceReportRequest.submitType
        this.updatedAt = LocalDateTime.now()
        this.leaveDateTime = submitAbsenceReportRequest.leaveDateTime
        this.lateDateTime = submitAbsenceReportRequest.lateDateTime
    }
}