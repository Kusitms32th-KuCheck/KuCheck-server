package onku.backend.point.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import onku.backend.domain.attendance.Attendance
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.ManualPoint
import onku.backend.domain.point.dto.AdminPointOverviewDto
import onku.backend.domain.point.dto.MemberMonthlyAttendanceDto
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.point.service.AdminPointService
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.page.PageResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class AdminPointServiceTest {

    @MockK lateinit var memberProfileRepository: MemberProfileRepository
    @MockK lateinit var kupickRepository: KupickRepository
    @MockK lateinit var manualPointRecordRepository: ManualPointRepository
    @MockK lateinit var sessionRepository: SessionRepository
    @MockK lateinit var memberPointHistoryRepository: MemberPointHistoryRepository
    @MockK lateinit var attendanceRepository: AttendanceRepository

    lateinit var clock: Clock
    lateinit var service: AdminPointService

    @BeforeEach
    fun setUp() {
        clock = Clock.fixed(
            Instant.parse("2025-01-01T01:00:00Z"), // Asia/Seoul → 10:00
            ZoneId.of("Asia/Seoul")
        )

        service = AdminPointService(
            memberProfileRepository = memberProfileRepository,
            kupickRepository = kupickRepository,
            manualPointRecordRepository = manualPointRecordRepository,
            sessionRepository = sessionRepository,
            memberPointHistoryRepository = memberPointHistoryRepository,
            attendanceRepository = attendanceRepository,
            clock = clock
        )
    }

    private fun createMember(
        id: Long = 1L,
        isTf: Boolean = false,
        isStaff: Boolean = false
    ): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        every { m.isTf } returns isTf
        every { m.isStaff } returns isStaff
        return m
    }

    private fun createProfile(
        memberId: Long,
        member: Member,
        name: String = "홍길동",
        part: Part = Part.BACKEND,
        phone: String? = "010-0000-0000",
        school: String? = "K대학교",
        major: String? = "컴퓨터공학"
    ): MemberProfile {
        val p = mockk<MemberProfile>(relaxed = true)
        every { p.memberId } returns memberId
        every { p.member } returns member
        every { p.name } returns name
        every { p.part } returns part
        every { p.phoneNumber } returns phone
        every { p.school } returns school
        every { p.major } returns major
        return p
    }

    // getAdminOverview

    @Test
    fun `getAdminOverview - 한 명에 대해 월별 출석, 큐픽 참여, 수동 포인트를 잘 매핑한다`() {
        val year = 2025
        val page = 0
        val size = 10
        val pageable = PageRequest.of(page, size)

        val member = createMember(id = 1L, isTf = true, isStaff = false)
        val profile = createProfile(memberId = 1L, member = member)

        every {
            memberProfileRepository.findAllByOrderByPartAscNameAsc(any())
        } returns PageImpl(listOf(profile), pageable, 1)

        // 1) 출석 포인트 합산 결과 (8~12월 중 9월만 30점 있다고 가정)
        every {
            memberPointHistoryRepository.sumAttendanceByMemberAndMonth(
                memberIds = listOf(1L),
                category = any(),
                start = any(),
                end = any()
            )
        } returns listOf(
            mockk {
                every { getMemberId() } returns 1L
                every { getMonth() } returns 9
                every { getPoints() } returns 30L
            }
        )

        // 2) 큐픽 참여 여부 (8~12 중 10월에만 참여)
        every {
            kupickRepository.findMemberMonthParticipation(
                listOf(1L),
                any(),
                any()
            )
        } returns listOf(
            arrayOf<Any>(1L, 10) // memberId, month
        )

        // 3) 수동 포인트 (study = 5, kuporters = 7)
        val manual = mockk<ManualPoint>()

        every { manual.memberId } returns 1L
        every { manual.studyPoints } returns 5
        every { manual.kupportersPoints } returns 7
        every { manual.memo } returns "열심히 함"
        every { manual.member } returns member   // isTf, isStaff 볼 수도 있으니 안전하게

        every {
            manualPointRecordRepository.findByMemberIdIn(listOf(1L))
        } returns listOf(manual)

        val response: PageResponse<AdminPointOverviewDto> =
            service.getAdminOverview(year = year, page = page, size = size)

        assertEquals(1, response.data.size)

        val dto = response.data[0]
        assertEquals(1L, dto.memberId)
        assertEquals("홍길동", dto.name)
        assertEquals(Part.BACKEND, dto.part)
        assertEquals("010-0000-0000", dto.phoneNumber)
        assertEquals("K대학교", dto.school)
        assertEquals("컴퓨터공학", dto.major)
        assertTrue(dto.isTf)
        assertFalse(dto.isStaff)

        // 8~12월 키가 모두 있어야 함
        assertEquals(setOf(8, 9, 10, 11, 12), dto.attendanceMonthlyTotals.keys)
        // 9월만 30점
        assertEquals(30, dto.attendanceMonthlyTotals[9])
        assertEquals(0, dto.attendanceMonthlyTotals[8])
        assertEquals(0, dto.attendanceMonthlyTotals[10])

        // 큐픽 참여: 10월만 true
        assertEquals(setOf(8, 9, 10, 11, 12), dto.kupickParticipation.keys)
        assertEquals(true, dto.kupickParticipation[10])
        assertEquals(false, dto.kupickParticipation[8])

        // 수동 포인트
        assertEquals(5, dto.studyPoints)
        assertEquals(7, dto.kuportersPoints)
        assertEquals("열심히 함", dto.memo)
    }

    @Test
    fun `getAdminOverview - 멤버가 아예 없으면 빈 페이지 반환`() {
        val pageable = PageRequest.of(0, 10)
        every {
            memberProfileRepository.findAllByOrderByPartAscNameAsc(any())
        } returns PageImpl(emptyList(), pageable, 0)

        val response = service.getAdminOverview(
            year = 2025,
            page = 0,
            size = 10
        )

        assertTrue(response.data.isEmpty())
    }

    // getMonthlyPaged

    @Test
    fun `getMonthlyPaged - 해당 월 세션이 하나도 없으면 sessionDates와 members 비어있다`() {
        every {
            sessionRepository.findByStartDateBetween(any(), any())
        } returns emptyList()

        val result = service.getMonthlyPaged(
            year = 2025,
            month = 1,
            page = 0,
            size = 10
        )

        assertEquals(2025, result.year)
        assertEquals(1, result.month)
        assertTrue(result.sessionDates.isEmpty())
        assertTrue(result.members.data.isEmpty())
    }

    @Test
    fun `getMonthlyPaged - 세션 2개, 출석 1개만 있을 때 나머지 날짜는 status null로 채운다`() {
        val year = 2025
        val month = 1

        // 세션 2개: 1월 5일, 1월 20일
        val session1 = mockk<Session>(relaxed = true)
        every { session1.id } returns 10L
        every { session1.startDate } returns LocalDate.of(year, month, 5)

        val session2 = mockk<Session>(relaxed = true)
        every { session2.id } returns 11L
        every { session2.startDate } returns LocalDate.of(year, month, 20)

        every {
            sessionRepository.findByStartDateBetween(any(), any())
        } returns listOf(session1, session2)

        // 페이지 멤버 1명
        val pageable = PageRequest.of(0, 10)
        val member = createMember(1L)
        val profile = createProfile(memberId = 1L, member = member, name = "테스터")

        every {
            memberProfileRepository.findAllByOrderByPartAscNameAsc(pageable)
        } returns PageImpl(listOf(profile), pageable, 1)

        // 출석: 이 멤버는 1월 5일(세션1)에만 PRESENT
        val attendance = mockk<Attendance>(relaxed = true)
        every { attendance.id } returns 100L
        every { attendance.memberId } returns 1L
        every { attendance.sessionId } returns 10L
        every { attendance.status } returns AttendancePointType.PRESENT
        every { attendance.attendanceTime } returns LocalDateTime.of(year, month, 5, 10, 0)

        every {
            attendanceRepository.findByMemberIdInAndAttendanceTimeBetween(
                listOf(1L),
                any(),
                any()
            )
        } returns listOf(attendance)

        val result = service.getMonthlyPaged(
            year = year,
            month = month,
            page = 0,
            size = 10
        )

        assertEquals(year, result.year)
        assertEquals(month, result.month)
        // sessionDates = [5, 20]
        assertEquals(listOf(5, 20), result.sessionDates)

        // 멤버는 한 명
        assertEquals(1, result.members.data.size)
        val memberDto: MemberMonthlyAttendanceDto = result.members.data[0]
        assertEquals(1L, memberDto.memberId)
        assertEquals("테스터", memberDto.name)

        // records 는 날짜 2개에 대해 존재해야 한다
        assertEquals(2, memberDto.records.size)
        val rec1 = memberDto.records[0]
        val rec2 = memberDto.records[1]

        assertEquals(LocalDate.of(year, month, 5), rec1.date)
        assertEquals(AttendancePointType.PRESENT, rec1.status)
        assertEquals(AttendancePointType.PRESENT.points, rec1.point)

        assertEquals(LocalDate.of(year, month, 20), rec2.date)
        assertNull(rec2.status)
        assertNull(rec2.point)
    }

    @Test
    fun `getMonthlyPaged - 페이지에 멤버가 없으면 예외 발생`() {
        val year = 2025
        val month = 1

        // 세션은 있는데
        val session = mockk<Session>(relaxed = true)
        every { session.id } returns 10L
        every { session.startDate } returns LocalDate.of(year, month, 5)
        every {
            sessionRepository.findByStartDateBetween(any(), any())
        } returns listOf(session)

        // member 페이지는 비어있음
        val pageable = PageRequest.of(0, 10)
        every {
            memberProfileRepository.findAllByOrderByPartAscNameAsc(pageable)
        } returns PageImpl(emptyList(), pageable, 0)

        val ex = assertFailsWith<CustomException> {
            service.getMonthlyPaged(
                year = year,
                month = month,
                page = 0,
                size = 10
            )
        }

        assertEquals(MemberErrorCode.PAGE_MEMBERS_NOT_FOUND, ex.errorCode)
    }
}