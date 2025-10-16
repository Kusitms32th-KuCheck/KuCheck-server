package onku.backend.domain.absence

import jakarta.persistence.*
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceType
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import onku.backend.global.entity.BaseEntity

@Entity
class AbsenceReport(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "absence_report_id")
    val id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    val session : Session,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member : Member,

    @Column(name = "url")
    val url : String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    val status : AbsenceType,

    @Column(name = "reason")
    val reason : String,

    @Column(name = "approval")
    @Enumerated(EnumType.STRING)
    val approval : AbsenceReportApproval,
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
                status = submitAbsenceReportRequest.absenceType,
                reason = submitAbsenceReportRequest.reason,
                approval = AbsenceReportApproval.SUBMIT
            )
        }
    }
}