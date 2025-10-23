package onku.backend.domain.attendance.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import onku.backend.domain.attendance.AttendanceErrorCode
import onku.backend.domain.attendance.AttendancePolicy
import onku.backend.domain.attendance.dto.*
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.member.Member
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.redis.cache.AttendanceTokenCache
import onku.backend.global.util.TokenGenerator
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
class AttendanceService(
    private val tokenCache: AttendanceTokenCache,
    private val sessionRepository: SessionRepository,
    private val attendanceRepository: AttendanceRepository,
    private val memberProfileRepository: MemberProfileRepository,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    private val tokenGenerator: TokenGenerator,
    @PersistenceContext private val em: EntityManager,
    private val clock: Clock
) {
    @Transactional(readOnly = true)
    fun issueAttendanceTokenFor(member: Member): AttendanceTokenResponse {
        val now = LocalDateTime.now(clock)
        val expAt = now.plusSeconds(AttendancePolicy.TOKEN_TTL_SECONDS)
        val token = tokenGenerator.generateOpaqueToken()

        tokenCache.putAsActiveSingle(
            member.id!!,
            token,
            now,
            expAt,
            AttendancePolicy.TOKEN_TTL_SECONDS
        )
        return AttendanceTokenResponse(token = token, expAt = expAt)
    }

    private fun findOpenSession(now: LocalDateTime): Session? {
        val startUpper = now.plusMinutes(AttendancePolicy.OPEN_GRACE_MINUTES)
        val endLower = now
        return sessionRepository
            .findFirstByStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByStartTimeDesc(
                startUpper, endLower
            )
    }

    @Transactional
    fun scanAndRecordBy(admin: Member, token: String): AttendanceResponse {
        val now = LocalDateTime.now(clock)

        val session = findOpenSession(now)
            ?: throw CustomException(AttendanceErrorCode.SESSION_NOT_OPEN)

        val peek = tokenCache.peek(token)
            ?: throw CustomException(AttendanceErrorCode.TOKEN_INVALID)

        val memberId = peek.memberId
        val memberName = memberProfileRepository.findById(memberId).orElse(null)?.name ?: "Unknown"

        if (attendanceRepository.existsBySessionIdAndMemberId(session.id!!, memberId)) {
            throw CustomException(AttendanceErrorCode.ATTENDANCE_ALREADY_RECORDED)
        }

        tokenCache.consumeToken(token) ?: throw CustomException(ErrorCode.UNAUTHORIZED)

        val startTime = session.startTime
        val lateThreshold = startTime.plusMinutes(AttendancePolicy.LATE_WINDOW_MINUTES)

        val state = when {
            now.isAfter(lateThreshold) -> AttendancePointType.ABSENT
            !now.isBefore(startTime)   -> AttendancePointType.LATE
            else                       -> AttendancePointType.PRESENT
        }

        try {
            attendanceRepository.insertOnly(
                sessionId = session.id!!,
                memberId = memberId,
                status = state.name,
                attendanceTime = now,
                createdAt = now,
                updatedAt = now
            )

            val memberRef = em.getReference(Member::class.java, memberId)
            val week: Long = session.week
            val history = MemberPointHistory.ofAttendance(
                member = memberRef,
                status = state,
                occurredAt = now,
                week = week,
                time = now.toLocalTime()
            )
            memberPointHistoryRepository.save(history)

        } catch (e: DataIntegrityViolationException) {
            throw CustomException(AttendanceErrorCode.ATTENDANCE_ALREADY_RECORDED)
        }

        return AttendanceResponse(
            memberId = memberId,
            memberName = memberName,
            sessionId = session.id!!,
            state = state,
            scannedAt = now
        )
    }
}