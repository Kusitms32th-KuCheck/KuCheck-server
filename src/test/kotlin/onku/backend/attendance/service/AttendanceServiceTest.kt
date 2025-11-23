package onku.backend.attendance.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jakarta.persistence.EntityManager
import onku.backend.domain.attendance.AttendanceErrorCode
import onku.backend.domain.attendance.AttendancePolicy
import onku.backend.domain.attendance.dto.AttendanceAvailabilityResponse
import onku.backend.domain.attendance.dto.AttendanceTokenCore
import onku.backend.domain.attendance.enums.AttendanceAvailabilityReason
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.attendance.service.AttendanceService
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.util.SessionTimeUtil
import onku.backend.global.exception.CustomException
import onku.backend.global.redis.cache.AttendanceTokenCache
import onku.backend.global.redis.dto.TokenData
import onku.backend.global.util.TokenGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AttendanceServiceTest {
    @MockK(relaxUnitFun = true) lateinit var tokenCache: AttendanceTokenCache
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var attendanceRepository: AttendanceRepository
    @MockK lateinit var memberProfileRepository: MemberProfileRepository
    @MockK lateinit var memberPointHistoryRepository: MemberPointHistoryRepository
    @MockK lateinit var tokenGenerator: TokenGenerator
    @MockK lateinit var em: EntityManager


    lateinit var clock: Clock
    lateinit var attendanceService: AttendanceService

    @BeforeEach
    fun setUp() {
        // now = 2025-01-01T10:00:00 in Asia/Seoul
        clock = Clock.fixed(
            Instant.parse("2025-01-01T01:00:00Z"),
            ZoneId.of("Asia/Seoul")
        )

        attendanceService = AttendanceService(
            tokenCache = tokenCache,
            sessionRepository = sessionRepository,
            attendanceRepository = attendanceRepository,
            memberProfileRepository = memberProfileRepository,
            memberPointHistoryRepository = memberPointHistoryRepository,
            tokenGenerator = tokenGenerator,
            em = em,
            clock = clock
        )

        mockkStatic(SessionTimeUtil::class)
    }

    private fun createMember(id: Long = 1L): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        return m
    }

    private fun createSession(
        id: Long = 10L,
        week: Long = 3L,
        isHoliday: Boolean = false
    ): Session {
        val s = mockk<Session>(relaxed = true)
        every { s.id } returns id
        every { s.week } returns week
        every { s.isHoliday } returns isHoliday
        return s
    }

    private fun stubMemberProfile(memberId: Long, name: String = "테스트유저") {
        val profile = mockk<MemberProfile>()
        every { profile.name } returns name
        every { memberProfileRepository.findById(memberId) } returns Optional.of(profile)
    }

    // ==========================
    // 1) issueAttendanceTokenFor
    // ==========================
    @Test
    fun `issueAttendanceTokenFor - 토큰 발급 및 캐시에 저장`() {
        val member = createMember(1L)
        val now = LocalDateTime.now(clock)
        val ttl = AttendancePolicy.TOKEN_TTL_SECONDS
        val fakeToken = "opaque-token-123"

        every { tokenGenerator.generateOpaqueToken() } returns fakeToken

        stubMemberProfile(member.id!!)

        val result: AttendanceTokenCore = attendanceService.issueAttendanceTokenFor(member)

        assertEquals(fakeToken, result.token)
        assertEquals(now.plusSeconds(ttl), result.expAt)

        verify {
            tokenCache.putAsActiveSingle(
                1L,
                "opaque-token-123",
                now,
                now.plusSeconds(ttl),
                ttl,
                null
            )
        }
    }

    // ==========================
    // 2) scanAndRecordBy - 정상 출석 기록
    // ==========================
    @Test
    fun `scanAndRecordBy - 정상 출석 기록 및 포인트 적립`() {
        val admin = createMember(id = 99L)
        val member = createMember(id = 1L)
        val token = "token-abc"

        val now = LocalDateTime.now(clock)
        val session = createSession(id = 10L, week = 2L, isHoliday = false)

        // 세션 열려있음 (findOpenSession 내부: findOpenWindow(startBound, now))
        every {
            sessionRepository.findOpenWindow(any(), now)
        } returns listOf(session)

        // 👉 TokenData 실제 인스턴스 생성
        val peekResult = TokenData(
            memberId = member.id!!,
            issuedAt = now.minusSeconds(10),
            expAt = now.plusSeconds(100),
            used = false
        )

        // tokenCache.peek 는 이 TokenData 리턴
        every { tokenCache.peek(token) } returns peekResult

        // 아직 출석 안 되어 있음
        every {
            attendanceRepository.existsBySessionIdAndMemberId(any(), any())
        } returns false

        // 토큰 소비 시에도 같은 TokenData 리턴
        every { tokenCache.consumeToken(token, any()) } returns peekResult

        // SessionTimeUtil.startDateTime(session) mock
        mockkObject(SessionTimeUtil)
        every { SessionTimeUtil.startDateTime(session) } returns now

        // insertOnly: 어떤 값이 오든 1 리턴
        every {
            attendanceRepository.insertOnly(
                any(), any(), any(), any(), any(), any()
            )
        } returns 1

        // getReference: MemberProxy 역할만 하면 되니까 any()로
        val memberRef = mockk<Member>()
        every { em.getReference(Member::class.java, any()) } returns memberRef

        every { memberPointHistoryRepository.save(any()) } answers { firstArg() }

        // 이번 주 요약은 countGroupedByStatusBetweenDates 를 빈 리스트로만 처리
        every {
            attendanceRepository.countGroupedByStatusBetweenDates(any(), any())
        } returns emptyList()

        // 프로필 이름 stub
        stubMemberProfile(member.id!!)

        // when
        val response = attendanceService.scanAndRecordBy(admin, token)

        // then
        assertEquals(member.id!!, response.memberId)
        assertEquals(session.id!!, response.sessionId)
        assertEquals(AttendancePointType.LATE, response.state)
        assertEquals(now, response.scannedAt)

        verify(exactly = 1) {
            attendanceRepository.insertOnly(
                any(),               // sessionId
                any(),               // memberId
                AttendancePointType.LATE.name,
                any(), any(), any()
            )
        }

        // save는 any()로 한 번 호출됐는지만 검증
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
    }

    // ==========================
    // 3) scanAndRecordBy - 세션 안 열렸을 때 예외
    // ==========================
    @Test
    fun `scanAndRecordBy - 열려있는 세션이 없으면 예외`() {
        val admin = createMember(99L)
        val token = "token-x"
        val now = LocalDateTime.now(clock)

        every { sessionRepository.findOpenWindow(any(), now) } returns emptyList()

        val ex = assertThrows<CustomException> {
            attendanceService.scanAndRecordBy(admin, token)
        }
        assertEquals(AttendanceErrorCode.SESSION_NOT_OPEN, ex.errorCode)
    }

    // ==========================
    // 4) checkAvailabilityFor
    // ==========================
    @Test
    fun `checkAvailabilityFor - 세션 없으면 available false`() {
        val member = createMember(1L)
        val now = LocalDateTime.now(clock)

        every { sessionRepository.findOpenWindow(any(), now) } returns emptyList()

        val result: AttendanceAvailabilityResponse =
            attendanceService.checkAvailabilityFor(member)

        assertEquals(false, result.available)
        assertEquals(AttendanceAvailabilityReason.NO_OPEN_SESSION, result.reason)
    }

    @Test
    fun `checkAvailabilityFor - 이미 출석했으면 available false`() {
        val member = createMember(1L)
        val session = createSession(id = 10L)

        // 세션 하나 열려있다고 가정
        every {
            sessionRepository.findOpenWindow(any(), any())
        } returns listOf(session)

        // 해당 세션에 이미 출석 기록 있음
        every {
            attendanceRepository.existsBySessionIdAndMemberId(any(), any())
        } returns true

        val result = attendanceService.checkAvailabilityFor(member)

        assertEquals(false, result.available)
        assertEquals(AttendanceAvailabilityReason.ALREADY_RECORDED, result.reason)
    }

    @Test
    fun `checkAvailabilityFor - 출석 가능`() {
        val member = createMember(1L)
        val now = LocalDateTime.now(clock)
        val session = createSession(id = 10L)

        every { sessionRepository.findOpenWindow(any(), now) } returns listOf(session)
        every { attendanceRepository.existsBySessionIdAndMemberId(any(), any()) } returns false

        val result = attendanceService.checkAvailabilityFor(member)

        assertTrue(result.available)
        assertEquals(null, result.reason)
    }
}