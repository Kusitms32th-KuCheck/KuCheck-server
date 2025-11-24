package onku.backend.point.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.enums.AbsenceApprovedType
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.util.AbsenceReportToAttendancePointMapper
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.converter.MemberPointConverter
import onku.backend.domain.point.dto.MemberPointHistoryResponse
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.point.service.MemberPointHistoryService
import onku.backend.domain.session.Session
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class MemberPointHistoryServiceTest {

    @MockK lateinit var recordRepository: MemberPointHistoryRepository
    @MockK lateinit var memberProfileRepository: MemberProfileRepository

    lateinit var service: MemberPointHistoryService

    @BeforeEach
    fun setUp() {
        service = MemberPointHistoryService(
            recordRepository = recordRepository,
            memberProfileRepository = memberProfileRepository
        )
    }

    private fun createMember(id: Long = 1L): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        return m
    }

    private fun createProfile(member: Member, name: String? = "홍길동"): MemberProfile {
        val p = mockk<MemberProfile>(relaxed = true)
        every { p.memberId } returns member.id
        every { p.member } returns member
        every { p.name } returns name
        return p
    }

    // ==========================
    // getHistory
    // ==========================

    @Test
    fun `getHistory - 누적 포인트와 기록 페이지를 잘 매핑한다`() {
        val member = createMember(1L)
        val pageIndex = 0
        val size = 10
        val pageable = PageRequest.of(pageIndex, size)

        val profile = createProfile(member, "김테스터")
        every { memberProfileRepository.findById(1L) } returns java.util.Optional.of(profile)

        // 합계 projection mock
        val sums = mockk<MemberPointHistoryRepository.MemberPointSums> {
            every { getPlusPoints() } returns 100L
            every { getMinusPoints() } returns 20L
            every { getTotalPoints() } returns 80L
        }
        every { recordRepository.sumPointsForMember(member) } returns sums

        // 히스토리 엔티티 + 페이지
        val history1 = mockk<MemberPointHistory>(relaxed = true)
        val history2 = mockk<MemberPointHistory>(relaxed = true)

        every {
            recordRepository.findByMemberOrderByOccurredAtDesc(member, pageable)
        } returns PageImpl(listOf(history1, history2), pageable, 2)

        // Converter mock
        mockkObject(MemberPointConverter)
        val dto1 = mockk<onku.backend.domain.point.dto.MemberPointHistory>()
        val dto2 = mockk<onku.backend.domain.point.dto.MemberPointHistory>()

        every { MemberPointConverter.toResponse(history1) } returns dto1
        every { MemberPointConverter.toResponse(history2) } returns dto2

        // when
        val response: MemberPointHistoryResponse =
            service.getHistory(member, pageIndex, size)

        // then
        assertEquals(1L, response.memberId)
        assertEquals("김테스터", response.name)
        assertEquals(100, response.plusPoints)
        assertEquals(20, response.minusPoints)
        assertEquals(80, response.totalPoints)

        assertEquals(2, response.records.size)
        assertSame(dto1, response.records[0])
        assertSame(dto2, response.records[1])

        assertEquals(1, response.totalPages)   // totalElements=2, size=10 → totalPages=1
        assertTrue(response.isLastPage)

        verify { memberProfileRepository.findById(1L) }
        verify { recordRepository.sumPointsForMember(member) }
        verify { recordRepository.findByMemberOrderByOccurredAtDesc(member, pageable) }
    }

    @Test
    fun `getHistory - 프로필이 없으면 MEMBER_NOT_FOUND 예외`() {
        val member = createMember(1L)
        every { memberProfileRepository.findById(1L) } returns java.util.Optional.empty()

        val ex = assertFailsWith<CustomException> {
            service.getHistory(member, safePage = 0, size = 10)
        }

        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
        verify { memberProfileRepository.findById(1L) }
        verify(exactly = 0) { recordRepository.sumPointsForMember(any()) }
    }

    // ==========================
    // upsertPointFromAbsenceReport
    // ==========================

    @Test
    fun `upsertPointFromAbsenceReport - 기존 기록 없으면 새로 생성해서 저장`() {
        val member = createMember(1L)
        val session = mockk<Session>(relaxed = true)
        every { session.week } returns 3L

        val absence = mockk<AbsenceReport>(relaxed = true)
        every { absence.member } returns member
        every { absence.session } returns session
        every { absence.approval } returns AbsenceReportApproval.APPROVED
        every { absence.approvedType } returns AbsenceApprovedType.EXCUSED

        every {
            recordRepository.findByWeekAndMember(3L, member)
        } returns null

        // mapper + factory mock
        mockkObject(AbsenceReportToAttendancePointMapper)
        every {
            AbsenceReportToAttendancePointMapper.map(
                AbsenceReportApproval.APPROVED,
                AbsenceApprovedType.EXCUSED
            )
        } returns AttendancePointType.EXCUSED

        mockkObject(MemberPointHistory)
        val newHistory = mockk<MemberPointHistory>(relaxed = true)

        every {
            MemberPointHistory.ofAttendance(
                member = member,
                status = AttendancePointType.EXCUSED,
                occurredAt = any(),
                week = 3L
            )
        } returns newHistory

        every { recordRepository.save(newHistory) } returns newHistory

        // when
        service.upsertPointFromAbsenceReport(absence)

        // then
        verify {
            recordRepository.findByWeekAndMember(3L, member)
        }
        verify {
            AbsenceReportToAttendancePointMapper.map(
                AbsenceReportApproval.APPROVED,
                AbsenceApprovedType.EXCUSED
            )
        }
        verify {
            MemberPointHistory.ofAttendance(
                member = member,
                status = AttendancePointType.EXCUSED,
                occurredAt = any(),
                week = 3L
            )
        }
        verify { recordRepository.save(newHistory) }
    }

    @Test
    fun `upsertPointFromAbsenceReport - 기존 기록 있으면 updateAttendancePointType 호출 후 save`() {
        val member = createMember(1L)
        val session = mockk<Session>(relaxed = true)
        every { session.week } returns 4L

        val absence = mockk<AbsenceReport>(relaxed = true)
        every { absence.member } returns member
        every { absence.session } returns session
        every { absence.approval } returns AbsenceReportApproval.APPROVED
        every { absence.approvedType } returns AbsenceApprovedType.ABSENT

        val existingHistory = mockk<MemberPointHistory>(relaxed = true)

        every {
            recordRepository.findByWeekAndMember(4L, member)
        } returns existingHistory

        mockkObject(AbsenceReportToAttendancePointMapper)
        every {
            AbsenceReportToAttendancePointMapper.map(
                AbsenceReportApproval.APPROVED,
                AbsenceApprovedType.ABSENT
            )
        } returns AttendancePointType.ABSENT

        every {
            existingHistory.updateAttendancePointType(
                status = AttendancePointType.ABSENT,
                occurredAt = any()
            )
        } just Runs

        every { recordRepository.save(existingHistory) } returns existingHistory

        // when
        service.upsertPointFromAbsenceReport(absence)

        // then
        verify {
            recordRepository.findByWeekAndMember(4L, member)
        }
        verify {
            AbsenceReportToAttendancePointMapper.map(
                AbsenceReportApproval.APPROVED,
                AbsenceApprovedType.ABSENT
            )
        }
        verify {
            existingHistory.updateAttendancePointType(
                status = AttendancePointType.ABSENT,
                occurredAt = any()
            )
        }
        verify { recordRepository.save(existingHistory) }

        // 새로 만드는 ofAttendance는 호출되면 안 됨
        mockkObject(MemberPointHistory)
        verify(exactly = 0) {
            MemberPointHistory.ofAttendance(any(), any(), any(), any())
        }
    }
}