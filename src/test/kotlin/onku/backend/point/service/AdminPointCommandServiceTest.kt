package onku.backend.point.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import onku.backend.domain.attendance.Attendance
import onku.backend.domain.attendance.AttendanceErrorCode
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.ManualPoint
import onku.backend.domain.point.dto.StudyPointsResult
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.point.service.AdminPointCommandService
import onku.backend.domain.session.Session
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.util.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class AdminPointCommandServiceTest {

    @MockK lateinit var manualPointRecordRepository: ManualPointRepository
    @MockK lateinit var memberRepository: MemberRepository
    @MockK lateinit var kupickRepository: KupickRepository
    @MockK lateinit var memberPointHistoryRepository: MemberPointHistoryRepository
    @MockK lateinit var attendanceRepository: AttendanceRepository
    @MockK lateinit var sessionRepository: SessionRepository

    lateinit var clock: Clock
    lateinit var service: AdminPointCommandService

    @BeforeEach
    fun setUp() {
        clock = Clock.fixed(
            Instant.parse("2025-01-01T01:00:00Z"), // Asia/Seoul → 10:00
            ZoneId.of("Asia/Seoul")
        )

        service = AdminPointCommandService(
            manualPointRecordRepository = manualPointRecordRepository,
            memberRepository = memberRepository,
            kupickRepository = kupickRepository,
            memberPointHistoryRepository = memberPointHistoryRepository,
            attendanceRepository = attendanceRepository,
            sessionRepository = sessionRepository,
            clock = clock
        )

        every { memberPointHistoryRepository.save(any()) } answers { firstArg() }
    }

    private fun createMember(id: Long = 1L): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        return m
    }

    // updateStudyPoints

    @Test
    fun `updateStudyPoints - 기존 레코드 있고 diff가 있으면 history 저장 + 값 갱신`() {
        val member = createMember(1L)
        val manual = ManualPoint(
            member = member,
            studyPoints = 10,
            kupportersPoints = 0,
            memo = null
        )

        every { manualPointRecordRepository.findByMemberId(1L) } returns manual
        every { manualPointRecordRepository.save(any()) } answers { firstArg() }

        val result: StudyPointsResult = service.updateStudyPoints(1L, 30)

        assertEquals(1L, result.memberId)
        assertEquals(30, result.studyPoints)
        assertEquals(30, manual.studyPoints)

        // diff = 20 → history 1번 저장
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
        verify(exactly = 1) { manualPointRecordRepository.save(manual) }
    }

    @Test
    fun `updateStudyPoints - diff가 0이면 history는 저장하지 않는다`() {
        val member = createMember(1L)
        val manual = ManualPoint(
            member = member,
            studyPoints = 20,
            kupportersPoints = 0,
            memo = null
        )

        every { manualPointRecordRepository.findByMemberId(1L) } returns manual
        every { manualPointRecordRepository.save(any()) } answers { firstArg() }

        val result = service.updateStudyPoints(1L, 20)

        assertEquals(20, result.studyPoints)
        assertEquals(20, manual.studyPoints)

        // history 저장 X
        verify(exactly = 0) { memberPointHistoryRepository.save(any()) }
        verify(exactly = 1) { manualPointRecordRepository.save(manual) }
    }

    @Test
    fun `updateStudyPoints - 기존 레코드 없으면 newManualRecord 생성해서 사용`() {
        val member = createMember(1L)

        every { manualPointRecordRepository.findByMemberId(1L) } returns null
        every { memberRepository.getReferenceById(1L) } returns member
        every { manualPointRecordRepository.save(any()) } answers { firstArg() }

        val result = service.updateStudyPoints(1L, 15)

        assertEquals(1L, result.memberId)
        assertEquals(15, result.studyPoints)

        // getReferenceById 통해 ManualPoint 생성
        verify { memberRepository.getReferenceById(1L) }
        verify { manualPointRecordRepository.save(any()) }
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) } // diff = 15
    }

    // updateKupportersPoints & updateMemo

    @Test
    fun `updateKupportersPoints - kupporters 포인트 변경 시 history와 레코드 갱신`() {
        val member = createMember(1L)
        val manual = ManualPoint(
            member = member,
            studyPoints = 0,
            kupportersPoints = 5,
            memo = null
        )

        every { manualPointRecordRepository.findByMemberId(1L) } returns manual
        every { manualPointRecordRepository.save(any()) } answers { firstArg() }

        val result = service.updateKupportersPoints(1L, 8)

        assertEquals(1L, result.memberId)
        assertEquals(8, result.kupportersPoints)
        assertEquals(8, manual.kupportersPoints)
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
    }

    @Test
    fun `updateMemo - 메모만 변경하고 history는 기록하지 않는다`() {
        val member = createMember(1L)
        val manual = ManualPoint(
            member = member,
            studyPoints = 0,
            kupportersPoints = 0,
            memo = "old"
        )

        every { manualPointRecordRepository.findByMemberId(1L) } returns manual
        every { manualPointRecordRepository.save(any()) } answers { firstArg() }

        val result = service.updateMemo(1L, "new memo")

        assertEquals(1L, result.memberId)
        assertEquals("new memo", result.memo)
        assertEquals("new memo", manual.memo)
        verify(exactly = 0) { memberPointHistoryRepository.save(any()) }
    }

    // updateIsTf

    @Test
    fun `updateIsTf - false에서 true로 토글되면 TF 포인트 추가되고 true 반환`() {
        val member = createMember(1L)
        every { member.isTf } returns false
        every { member.isTf = any() } just Runs
        every { memberRepository.findById(1L) } returns Optional.of(member)

        val result = service.updateIsTf(1L)

        assertTrue(result)
        verify { member.isTf = true }
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
    }

    @Test
    fun `updateIsTf - 회원 없으면 예외`() {
        every { memberRepository.findById(1L) } returns Optional.empty()

        val ex = assertFailsWith<CustomException> {
            service.updateIsTf(1L)
        }
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }

    // updateIsStaff
    @Test
    fun `updateIsStaff - false에서 true로 토글되면 STAFF 포인트 추가`() {
        val member = createMember(1L)
        every { member.isStaff } returns false
        every { member.isStaff = any() } just Runs
        every { memberRepository.findById(1L) } returns Optional.of(member)

        val result = service.updateIsStaff(1L)

        assertTrue(result)
        verify { member.isStaff = true }
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
    }

    // updateKupickApproval

    @Test
    fun `updateKupickApproval - 기존 Kupick이 있으면 approval 토글 및 history 기록`() {
        val member = createMember(1L)
        val targetYm = YearMonth.of(2025, 1)

        val kupick = mockk<Kupick>(relaxed = true)
        every { kupick.id } returns 10L
        every { kupick.approval } returns false
        every { kupick.updateApproval(any()) } just Runs

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every {
            kupickRepository.findThisMonthByMember(member, any(), any())
        } returns kupick
        every { kupickRepository.save(kupick) } returns kupick

        val result = service.updateKupickApproval(1L, targetYm)

        assertEquals(1L, result.memberId)
        assertEquals(10L, result.kupickId)
        assertTrue(result.isKupick)

        verify { kupick.updateApproval(true) }
        verify { memberPointHistoryRepository.save(any()) }
        verify { kupickRepository.save(kupick) }
    }

    @Test
    fun `updateKupickApproval - 회원 없으면 예외`() {
        every { memberRepository.findById(1L) } returns Optional.empty()

        val ex = assertFailsWith<CustomException> {
            service.updateKupickApproval(1L, YearMonth.of(2025, 1))
        }
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `updateKupickApproval - save 후 id null이면 예외`() {
        val member = createMember(1L)
        val kupick = mockk<Kupick>(relaxed = true)
        every { kupick.id } returns null

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { kupickRepository.findThisMonthByMember(member, any(), any()) } returns kupick
        every { kupickRepository.save(kupick) } returns kupick

        val ex = assertFailsWith<CustomException> {
            service.updateKupickApproval(1L, YearMonth.of(2025, 1))
        }
        assertEquals(KupickErrorCode.KUPICK_SAVE_FAILED, ex.errorCode)
    }

    // updateAttendanceAndHistory

    @Test
    fun `updateAttendanceAndHistory - attendance가 없으면 예외`() {
        every { attendanceRepository.findById(1L) } returns Optional.empty()

        val ex = assertFailsWith<CustomException> {
            service.updateAttendanceAndHistory(
                attendanceId = 1L,
                memberId = 1L,
                newStatus = AttendancePointType.LATE
            )
        }
        assertEquals(AttendanceErrorCode.ATTENDANCE_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `updateAttendanceAndHistory - attendance의 memberId가 다르면 예외`() {
        val attendance = mockk<Attendance>(relaxed = true)
        every { attendance.memberId } returns 2L
        every { attendanceRepository.findById(1L) } returns Optional.of(attendance)

        val ex = assertFailsWith<CustomException> {
            service.updateAttendanceAndHistory(
                attendanceId = 1L,
                memberId = 1L,
                newStatus = AttendancePointType.LATE
            )
        }
        assertEquals(AttendanceErrorCode.INVALID_MEMBER_FOR_ATTENDANCE, ex.errorCode)
    }

    @Test
    fun `updateAttendanceAndHistory - 상태가 같으면 저장 없이 diff 0으로 반환`() {
        val attendance = mockk<Attendance>(relaxed = true)
        every { attendance.memberId } returns 1L
        every { attendance.sessionId } returns 10L
        every { attendance.status } returns AttendancePointType.PRESENT

        val session = mockk<Session>(relaxed = true)
        every { session.week } returns 3L

        every { attendanceRepository.findById(1L) } returns Optional.of(attendance)
        every { sessionRepository.findById(10L) } returns Optional.of(session)

        val result = service.updateAttendanceAndHistory(
            attendanceId = 1L,
            memberId = 1L,
            newStatus = AttendancePointType.PRESENT
        )

        assertEquals(0, result.diff)
        assertEquals(AttendancePointType.PRESENT, result.oldStatus)
        assertEquals(AttendancePointType.PRESENT, result.newStatus)
        assertEquals(3L, result.week)

        verify(exactly = 0) { attendanceRepository.save(any()) }
        verify(exactly = 0) { memberPointHistoryRepository.save(any()) }
    }

    @Test
    fun `updateAttendanceAndHistory - 상태가 변경되면 attendance와 history 저장`() {
        val attendance = mockk<Attendance>(relaxed = true)
        every { attendance.memberId } returns 1L
        every { attendance.sessionId } returns 10L
        every { attendance.status } returns AttendancePointType.ABSENT
        every { attendance.status = any() } just Runs
        every { attendanceRepository.save(any()) } answers { firstArg() }

        val session = mockk<Session>(relaxed = true)
        every { session.week } returns 2L

        val member = createMember(1L)

        every { attendanceRepository.findById(1L) } returns Optional.of(attendance)
        every { sessionRepository.findById(10L) } returns Optional.of(session)
        every { memberRepository.findById(1L) } returns Optional.of(member)

        val result = service.updateAttendanceAndHistory(
            attendanceId = 1L,
            memberId = 1L,
            newStatus = AttendancePointType.PRESENT
        )

        // PRESENT.points - ABSENT.points 의 diff
        val expectedDiff = AttendancePointType.PRESENT.points - AttendancePointType.ABSENT.points

        assertEquals(AttendancePointType.ABSENT, result.oldStatus)
        assertEquals(AttendancePointType.PRESENT, result.newStatus)
        assertEquals(expectedDiff, result.diff)
        assertEquals(2L, result.week)

        verify { attendance.status = AttendancePointType.PRESENT }
        verify(exactly = 1) { attendanceRepository.save(attendance) }
        verify(exactly = 1) { memberPointHistoryRepository.save(any()) }
    }
}