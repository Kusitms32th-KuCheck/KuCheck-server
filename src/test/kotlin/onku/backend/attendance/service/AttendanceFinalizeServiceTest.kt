package onku.backend.attendance.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.persistence.EntityManager
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.attendance.service.AttendanceFinalizeService
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.util.SessionTimeUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AttendanceFinalizeServiceTest {

    @MockK
    lateinit var sessionRepository: SessionRepository

    @MockK
    lateinit var attendanceRepository: AttendanceRepository

    @MockK
    lateinit var absenceReportRepository: AbsenceReportRepository

    @MockK
    lateinit var memberRepository: MemberRepository

    @MockK
    lateinit var memberPointHistoryRepository: MemberPointHistoryRepository

    @MockK
    lateinit var em: EntityManager

    lateinit var clock: Clock

    lateinit var attendanceFinalizeService: AttendanceFinalizeService

    @BeforeEach
    fun setUp() {
        clock = Clock.fixed(
            Instant.parse("2025-01-01T09:00:00Z"),
            ZoneId.of("Asia/Seoul")
        )

        attendanceFinalizeService = AttendanceFinalizeService(
            sessionRepository = sessionRepository,
            attendanceRepository = attendanceRepository,
            absenceReportRepository = absenceReportRepository,
            memberRepository = memberRepository,
            memberPointHistoryRepository = memberPointHistoryRepository,
            em = em,
            clock = clock
        )

        mockkStatic(SessionTimeUtil::class)
        every { SessionTimeUtil.startDateTime(any()) } returns LocalDateTime.of(2025, 1, 1, 10, 0)
    }

    private fun createSession(
        id: Long = 1L,
        week: Long = 1L,
        finalized: Boolean = false
    ): Session {
        val session = mockk<Session>(relaxed = true)
        every { session.id } returns id
        every { session.week } returns week
        every { session.attendanceFinalized } returns finalized
        every { session.attendanceFinalized = any() } just Runs
        every { session.attendanceFinalizedAt = any<LocalDateTime>() } just Runs
        return session
    }

    @Test
    fun `finalizeSession - 이미 final 되었다면 바로 return`() {
        val session = createSession(finalized = true)
        every { sessionRepository.findById(1L) } returns java.util.Optional.of(session)

        attendanceFinalizeService.finalizeSession(1L)

        verify(exactly = 0) { memberRepository.findApprovedMemberIds() }
        verify(exactly = 0) { attendanceRepository.findMemberIdsBySessionId(any()) }
        verify(exactly = 0) { absenceReportRepository.findReportsBySessionAndMembers(any(), any()) }
    }

    @Test
    fun `finalizeSession - approved member가 없으면 finalize만 한다`() {
        val session = createSession(finalized = false)

        every { sessionRepository.findById(1L) } returns java.util.Optional.of(session)
        every { memberRepository.findApprovedMemberIds() } returns emptyList()

        attendanceFinalizeService.finalizeSession(1L)

        verify { session.attendanceFinalized = true }
        verify { session.attendanceFinalizedAt = LocalDateTime.now(clock) }
        verify(exactly = 0) { attendanceRepository.findMemberIdsBySessionId(any()) }
        verify(exactly = 0) { absenceReportRepository.findReportsBySessionAndMembers(any(), any()) }
    }

}