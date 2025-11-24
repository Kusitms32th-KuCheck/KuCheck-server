package onku.backend.session.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.session.Session
import onku.backend.domain.session.SessionDetail
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.response.ThisWeekSessionInfo
import onku.backend.domain.session.enums.SessionCategory
import onku.backend.domain.session.repository.SessionDetailRepository
import onku.backend.domain.session.repository.SessionImageRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.domain.session.service.SessionService
import onku.backend.domain.session.validator.SessionValidator
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.service.S3Service
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import java.time.*
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SessionServiceTest {

    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var sessionValidator: SessionValidator
    @MockK lateinit var sessionImageRepository: SessionImageRepository
    @MockK lateinit var s3Service: S3Service
    @MockK lateinit var attendanceRepository: AttendanceRepository
    @MockK lateinit var absenceReportRepository: AbsenceReportRepository
    @MockK lateinit var sessionDetailRepository: SessionDetailRepository

    lateinit var clock: Clock
    lateinit var sessionService: SessionService

    @BeforeEach
    fun setUp() {
        clock = Clock.fixed(
            Instant.parse("2025-01-01T01:00:00Z"), // Asia/Seoul → 2025-01-01T10:00
            ZoneId.of("Asia/Seoul")
        )

        sessionService = SessionService(
            sessionRepository = sessionRepository,
            sessionValidator = sessionValidator,
            sessionImageRepository = sessionImageRepository,
            s3Service = s3Service,
            clock = clock,
            attendanceRepository = attendanceRepository,
            absenceReportRepository = absenceReportRepository,
            sessionDetailRepository = sessionDetailRepository
        )
    }

    private fun createSession(
        id: Long = 1L,
        title: String = "세션",
        startDate: LocalDate = LocalDate.of(2025, 1, 5),
        week: Long = 1L,
        category: SessionCategory = SessionCategory.MEETUP_PROJECT,
        isHoliday: Boolean = false,
        detail: SessionDetail? = null
    ): Session {
        val s = mockk<Session>(relaxed = true)
        every { s.id } returns id
        every { s.title } returns title
        every { s.startDate } returns startDate
        every { s.week } returns week
        every { s.category } returns category
        every { s.isHoliday } returns isHoliday
        every { s.sessionDetail } returns detail
        return s
    }

    // getUpcomingSessionsForAbsence
    @Test
    fun `getUpcomingSessionsForAbsence - 세션 목록과 active 여부를 반환한다`() {
        val now = LocalDateTime.now(clock)           // 2025-01-01T10:00
        val today = now.toLocalDate()                // 2025-01-01

        val s1 = createSession(id = 1L, title = "1주차", startDate = today, week = 1L)
        val s2 = createSession(id = 2L, title = "2주차", startDate = today.plusDays(1), week = 2L)

        every { sessionRepository.findUpcomingSessions(today) } returns listOf(s1, s2)
        every { sessionValidator.isImminentSession(s1, now) } returns true
        every { sessionValidator.isImminentSession(s2, now) } returns false

        val result = sessionService.getUpcomingSessionsForAbsence()

        assertEquals(2, result.size)

        val r1 = result[0]
        assertEquals(1L, r1.sessionId)
        assertEquals("1주차", r1.title)
        assertEquals(1L, r1.week)
        assertEquals(true, r1.active)

        val r2 = result[1]
        assertEquals(2L, r2.sessionId)
        assertEquals("2주차", r2.title)
        assertEquals(2L, r2.week)
        assertEquals(false, r2.active)

        verify { sessionRepository.findUpcomingSessions(today) }
        verify { sessionValidator.isImminentSession(s1, now) }
        verify { sessionValidator.isImminentSession(s2, now) }
    }

    // getById
    @Test
    fun `getById - 세션이 있으면 반환한다`() {
        val session = createSession(id = 10L)
        every { sessionRepository.findByIdOrNull(10L) } returns session

        val result = sessionService.getById(10L)

        assertSame(session, result)
    }

    @Test
    fun `getById - 세션이 없으면 예외 던진다`() {
        every { sessionRepository.findByIdOrNull(10L) } returns null

        val ex = assertThrows<CustomException> {
            sessionService.getById(10L)
        }
        assertEquals(SessionErrorCode.SESSION_NOT_FOUND, ex.errorCode)
    }

    // saveAll
    @Test
    fun `saveAll - 요청 리스트를 Session 엔티티로 저장한다`() {
        val requests = listOf(
            SessionSaveRequest(
                title = "세션1",
                sessionDate = LocalDate.of(2025, 1, 10),
                category = SessionCategory.MEETUP_PROJECT,
                week = 1L,
                isHoliday = false
            ),
            SessionSaveRequest(
                title = "세션2",
                sessionDate = LocalDate.of(2025, 1, 17),
                category = SessionCategory.MEETUP_PROJECT,
                week = 2L,
                isHoliday = true
            )
        )

        every { sessionRepository.saveAll(any<Iterable<Session>>()) } answers {
            firstArg<Iterable<Session>>().toList()
        }

        val result = sessionService.saveAll(requests)
        assertTrue(result)

        val slot = slot<Iterable<Session>>()
        verify {
            sessionRepository.saveAll(capture(slot))
        }

        val saved = slot.captured.toList()
        assertEquals(2, saved.size)
        assertEquals("세션1", saved[0].title)
        assertEquals(LocalDate.of(2025, 1, 10), saved[0].startDate)
        assertEquals(1L, saved[0].week)
        assertFalse(saved[0].isHoliday)

        assertEquals("세션2", saved[1].title)
        assertEquals(LocalDate.of(2025, 1, 17), saved[1].startDate)
        assertEquals(2L, saved[1].week)
        assertTrue(saved[1].isHoliday)
    }

    // getInitialSession
    @Test
    fun `getInitialSession - 페이지로 세션을 조회하고 DTO로 매핑한다`() {
        val s1 = createSession(
            id = 1L,
            title = "세션1",
            startDate = LocalDate.of(2025, 1, 10),
            category = SessionCategory.MEETUP_PROJECT,
            isHoliday = false,
            detail = mockk(relaxed = true) {
                every { id } returns 100L
            }
        )
        val s2 = createSession(
            id = 2L,
            title = "세션2",
            startDate = LocalDate.of(2025, 1, 17),
            category = SessionCategory.MEETUP_PROJECT,
            isHoliday = true,
            detail = null
        )

        val pageable = PageRequest.of(0, 10)
        val page: Page<Session> = PageImpl(listOf(s1, s2), pageable, 2)

        every { sessionRepository.findAll(pageable) } returns page

        val resultPage = sessionService.getInitialSession(pageable)

        assertEquals(2, resultPage.totalElements)
        val dto1 = resultPage.content[0]
        assertEquals(1L, dto1.sessionId)
        assertEquals(LocalDate.of(2025, 1, 10), dto1.startDate)
        assertEquals("세션1", dto1.title)
        assertEquals(SessionCategory.MEETUP_PROJECT, dto1.category)
        assertEquals(100L, dto1.sessionDetailId)
        assertFalse(dto1.isHoliday)

        val dto2 = resultPage.content[1]
        assertEquals(2L, dto2.sessionId)
        assertEquals(LocalDate.of(2025, 1, 17), dto2.startDate)
        assertEquals("세션2", dto2.title)
        assertEquals(SessionCategory.MEETUP_PROJECT, dto2.category)
        assertNull(dto2.sessionDetailId)
        assertTrue(dto2.isHoliday)
    }

    // getThisWeekSession
    @Test
    fun `getThisWeekSession - 이번주 세션 정보를 매핑해서 반환한다`() {
        val projection = mockk<ThisWeekSessionInfo>()
        every { projection.sessionId } returns 1L
        every { projection.sessionDetailId } returns 10L
        every { projection.title } returns "이번주 세션"
        every { projection.place } returns "강의실 101"
        every { projection.startDate } returns LocalDate.of(2025, 1, 5)
        every { projection.startTime } returns LocalTime.of(10, 0)
        every { projection.endTime } returns LocalTime.of(12, 0)
        every { projection.isHoliday } returns false

        every { sessionRepository.findThisWeekSunToSat(any(), any()) } returns listOf(projection)

        val result = sessionService.getThisWeekSession()

        assertEquals(1, result.size)
        val dto = result[0]
        assertEquals(1L, dto.sessionId)
        assertEquals(10L, dto.sessionDetailId)
        assertEquals("이번주 세션", dto.title)
        assertEquals("강의실 101", dto.place)
        assertEquals(LocalDate.of(2025, 1, 5), dto.startDate)
        assertEquals(LocalTime.of(10, 0), dto.startTime)
        assertEquals(LocalTime.of(12, 0), dto.endTime)

        verify { sessionRepository.findThisWeekSunToSat(any(), any()) }
    }

    // getAllSessionsOrderByStartDate
    @Test
    fun `getAllSessionsOrderByStartDate - 모든 세션을 카드 정보로 반환한다`() {
        val s1 = createSession(
            id = 1L,
            title = "세션1",
            startDate = LocalDate.of(2025, 1, 3),
            category = SessionCategory.MEETUP_PROJECT,
            isHoliday = false
        )
        val s2 = createSession(
            id = 2L,
            title = "세션2",
            startDate = LocalDate.of(2025, 1, 10),
            category = SessionCategory.MEETUP_PROJECT,
            isHoliday = true
        )

        every { sessionRepository.findAllSessionsOrderByStartDate() } returns listOf(s1, s2)

        val result = sessionService.getAllSessionsOrderByStartDate()

        assertEquals(2, result.size)
        val card1 = result[0]
        assertEquals(1L, card1.sessionId)
        assertEquals(SessionCategory.MEETUP_PROJECT, card1.sessionCategory)
        assertEquals("세션1", card1.title)
        assertEquals(LocalDate.of(2025, 1, 3), card1.startDate)

        val card2 = result[1]
        assertEquals(2L, card2.sessionId)
        assertEquals(SessionCategory.MEETUP_PROJECT, card2.sessionCategory)
        assertEquals("세션2", card2.title)
        assertEquals(LocalDate.of(2025, 1, 10), card2.startDate)
    }

    // deleteCascade

    @Test
    fun `deleteCascade - 세션이 없으면 예외`() {
        every { sessionRepository.findWithDetail(1L) } returns null

        val ex = assertThrows<CustomException> {
            sessionService.deleteCascade(1L)
        }

        assertEquals(SessionErrorCode.SESSION_NOT_FOUND, ex.errorCode)
        verify(exactly = 0) { attendanceRepository.deleteAllBySessionId(any()) }
        verify(exactly = 0) { sessionRepository.deleteById(any()) }
    }

    @Test
    fun `deleteCascade - detailId가 없으면 attendance, absence만 지우고 세션 삭제`() {
        every { sessionRepository.findWithDetail(1L) } returns createSession(id = 1L)
        every { sessionRepository.findDetailIdBySessionId(1L) } returns null

        every { attendanceRepository.deleteAllBySessionId(1L) } returns 1
        every { absenceReportRepository.deleteAllBySessionId(1L) } returns 1
        every { sessionRepository.deleteById(1L) } just Runs

        sessionService.deleteCascade(1L)

        verify { attendanceRepository.deleteAllBySessionId(1L) }
        verify { absenceReportRepository.deleteAllBySessionId(1L) }
        verify(exactly = 0) { sessionImageRepository.findAllImageKeysByDetailId(any()) }
        verify(exactly = 0) { sessionDetailRepository.deleteById(any()) }
        verify { sessionRepository.deleteById(1L) }
    }

    @Test
    fun `deleteCascade - detailId가 있고 이미지가 있으면 S3와 이미지, detail, session을 차례로 삭제`() {
        val sessionId = 1L
        val detailId = 10L

        every { sessionRepository.findWithDetail(sessionId) } returns createSession(id = sessionId)
        every { sessionRepository.findDetailIdBySessionId(sessionId) } returns detailId

        every { attendanceRepository.deleteAllBySessionId(sessionId) } returns 1
        every { absenceReportRepository.deleteAllBySessionId(sessionId) } returns 1

        every { sessionImageRepository.findAllImageKeysByDetailId(detailId) } returns listOf(
            "",
            "SESSION/1/A/img1.png",
            "SESSION/1/A/img2.png"
        )

        every { s3Service.deleteObjectsNow(listOf("SESSION/1/A/img1.png", "SESSION/1/A/img2.png")) } just Runs
        every { sessionImageRepository.deleteByDetailIdBulk(detailId) } returns 1
        every { sessionRepository.detachDetailFromSession(sessionId) } returns 1
        every { sessionDetailRepository.deleteById(detailId) } just Runs
        every { sessionRepository.deleteById(sessionId) } just Runs

        sessionService.deleteCascade(sessionId)

        verify { attendanceRepository.deleteAllBySessionId(sessionId) }
        verify { absenceReportRepository.deleteAllBySessionId(sessionId) }
        verify { sessionImageRepository.findAllImageKeysByDetailId(detailId) }
        verify {
            s3Service.deleteObjectsNow(listOf("SESSION/1/A/img1.png", "SESSION/1/A/img2.png"))
        }
        verify { sessionImageRepository.deleteByDetailIdBulk(detailId) }
        verify { sessionRepository.detachDetailFromSession(sessionId) }
        verify { sessionDetailRepository.deleteById(detailId) }
        verify { sessionRepository.deleteById(sessionId) }
    }

    // getByDetailIdFetchDetail

    @Test
    fun `getByDetailIdFetchDetail - 세션이 있으면 반환`() {
        val session = createSession(id = 1L)
        every { sessionRepository.findByDetailIdFetchDetail(10L) } returns session

        val result = sessionService.getByDetailIdFetchDetail(10L)

        assertSame(session, result)
    }

    @Test
    fun `getByDetailIdFetchDetail - 세션 없으면 예외`() {
        every { sessionRepository.findByDetailIdFetchDetail(10L) } returns null

        val ex = assertThrows<CustomException> {
            sessionService.getByDetailIdFetchDetail(10L)
        }

        assertEquals(SessionErrorCode.SESSION_NOT_FOUND, ex.errorCode)
    }
}