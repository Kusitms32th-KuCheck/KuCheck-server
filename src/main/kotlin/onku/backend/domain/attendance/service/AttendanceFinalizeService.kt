package onku.backend.domain.attendance.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.attendance.AttendancePolicy
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.attendance.util.AbsenceReportToAttendancePointMapper
import onku.backend.domain.member.Member
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.util.SessionTimeUtil
import onku.backend.global.exception.CustomException
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
            .orElseThrow { CustomException(SessionErrorCode.SESSION_NOT_FOUND) }
        if (session.attendanceFinalized) return

        val startDateTime = SessionTimeUtil.startDateTime(session)
        val absentBoundary = startDateTime.plusMinutes(AttendancePolicy.ABSENT_START_MINUTES)

        val targetIds: Set<Long> = memberRepository.findApprovedMemberIds().toSet()
        if (targetIds.isEmpty()) { markFinalized(session, now); return }

        val recordedIds = attendanceRepository.findMemberIdsBySessionId(sessionId).toHashSet()
        val missing = targetIds - recordedIds

        if (missing.isNotEmpty()) {
            val papers = absenceReportRepository.findReportsBySessionAndMembers(sessionId, missing)
                .associateBy { it.member.id!! }

            missing.forEach { memberId ->
                val report = papers[memberId]
                val status: AttendancePointType =
                    report?.let { AbsenceReportToAttendancePointMapper.map(it.approval, it.approvedType) }
                        ?: AttendancePointType.ABSENT

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
                } catch (_: DataIntegrityViolationException) {
                }
            }
        }
        markFinalized(session, now)
    }

    private fun markFinalized(session: Session, now: LocalDateTime) {
        session.attendanceFinalized = true
        session.attendanceFinalizedAt = now
    }
}