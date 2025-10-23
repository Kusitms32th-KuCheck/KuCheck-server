package onku.backend.domain.attendance.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.attendance.AttendancePolicy
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.member.Member
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class AttendanceFinalizeService(
    private val sessionRepository: SessionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val absenceReportRepository: AbsenceReportRepository,
    private val memberRepository: MemberRepository,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    @PersistenceContext private val em: EntityManager,
    private val clock: Clock
) {
    @Transactional
    fun finalizeSession(sessionId: Long) {
        val now = LocalDateTime.now(clock)
        val session = sessionRepository.findById(sessionId)
            .orElseThrow { CustomException(ErrorCode.SESSION_NOT_FOUND) }
        if (session.attendanceFinalized) return

        val targetIds: Set<Long> = memberRepository.findApprovedMemberIds().toSet()
        if (targetIds.isEmpty()) { markFinalized(session, now); return }

        val recordedIds = attendanceRepository.findMemberIdsBySessionId(sessionId).toHashSet()
        val missing = targetIds - recordedIds

        if (missing.isNotEmpty()) {
            val papers = absenceReportRepository.findReportsBySessionAndMembers(sessionId, missing)
                .associateBy { it.member.id!! }
            val absentBoundary = session.startTime.plusMinutes(AttendancePolicy.ABSENT_START_MINUTES)

            missing.forEach { memberId ->
                val status = mapApprovalToStatus(papers[memberId]?.approval)
                try {
                    val inserted = attendanceRepository.insertOnly(
                        sessionId = sessionId,
                        memberId = memberId,
                        status = status.name,
                        attendanceTime = absentBoundary,
                        createdAt = now,
                        updatedAt = now
                    )
                    if (inserted > 0) {
                        val memberRef = em.getReference(Member::class.java, memberId)
                        val history = MemberPointHistory.ofAttendance(
                            member = memberRef,
                            status = status,
                            occurredAt = absentBoundary,
                            week = session.week
                        )
                        memberPointHistoryRepository.save(history)
                    }
                } catch (_: DataIntegrityViolationException) { }
            }
        }
        markFinalized(session, now)
    }

    private fun mapApprovalToStatus(approval: AbsenceReportApproval?): AttendancePointType =
        when (approval) {
            AbsenceReportApproval.APPROVED -> AttendancePointType.EXCUSED
            AbsenceReportApproval.SUBMIT   -> AttendancePointType.ABSENT_WITH_DOC
            null                           -> AttendancePointType.ABSENT
        }

    private fun markFinalized(session: Session, now: LocalDateTime) {
        session.attendanceFinalized = true
        session.attendanceFinalizedAt = now
    }
}
