package onku.backend.absence.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import onku.backend.domain.absence.AbsenceReport
import onku.backend.domain.absence.AbsenceReportErrorCode
import onku.backend.domain.absence.dto.request.SubmitAbsenceReportRequest
import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceSubmitType
import onku.backend.domain.absence.repository.AbsenceReportRepository
import onku.backend.domain.absence.repository.projection.GetMyAbsenceReportView
import onku.backend.domain.absence.service.AbsenceService
import onku.backend.domain.member.Member
import onku.backend.domain.session.Session
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class AbsenceServiceTest {

    @MockK
    lateinit var absenceReportRepository: AbsenceReportRepository

    lateinit var absenceService: AbsenceService

    @BeforeEach
    fun setUp() {
        absenceService = AbsenceService(absenceReportRepository)
    }

    private val member = mockk<Member>(relaxed = true)
    private val session = mockk<Session>(relaxed = true)
    private val fileKey = "test-file-key"

    // 새로운 결석신고 생성 테스트
    @Test
    fun `submitAbsenceReport - id가 없으면 새로 생성해서 save 한다`() {
        // given
        val request = SubmitAbsenceReportRequest(
            absenceReportId = null,
            sessionId = 1L,
            submitType = AbsenceSubmitType.ABSENT,
            reason = "테스트 이유",
            fileName = fileKey,
            null,
            null
        )

        mockkObject(AbsenceReport)
        val createdReport = mockk<AbsenceReport>()
        every {
            AbsenceReport.createAbsenceReport(
                member = member,
                session = session,
                submitAbsenceReportRequest = request,
                fileKey = fileKey
            )
        } returns createdReport

        every { absenceReportRepository.save(createdReport) } returns createdReport

        // when
        absenceService.submitAbsenceReport(member, request, fileKey, session)

        // then
        verify(exactly = 1) {
            AbsenceReport.createAbsenceReport(member, session, request, fileKey)
        }
        verify(exactly = 1) { absenceReportRepository.save(createdReport) }
        unmockkObject(AbsenceReport)
    }


    // 기존 결석신고 수정 테스트

    @Test
    fun `submitAbsenceReport - id가 있으면 기존 엔티티 update 후 save 한다`() {
        // given
        val request = SubmitAbsenceReportRequest(
            absenceReportId = 1L,
            sessionId = 1L,
            submitType = AbsenceSubmitType.ABSENT,
            reason = "테스트 이유",
            fileName = fileKey,
            null,
            null
        )

        val existingReport = mockk<AbsenceReport>(relaxed = true)

        every { absenceReportRepository.findById(1L) } returns java.util.Optional.of(existingReport)
        every { absenceReportRepository.save(existingReport) } returns existingReport

        // when
        absenceService.submitAbsenceReport(member, request, fileKey, session)

        // then
        verify(exactly = 1) {
            existingReport.updateAbsenceReport(request, fileKey, session)
        }
        verify(exactly = 1) { absenceReportRepository.save(existingReport) }
    }


    // getMyAbsenceReports 매핑 테스트
    @Test
    fun `getMyAbsenceReports - projection 리스트를 응답 DTO로 잘 매핑한다`() {
        val projection = mockk<GetMyAbsenceReportView>()
        every { projection.getAbsenceReportId() } returns 10L
        every { projection.getAbsenceSubmitType() } returns AbsenceSubmitType.LATE
        every { projection.getAbsenceReportApproval() } returns AbsenceReportApproval.APPROVED
        val now = LocalDateTime.now()
        every { projection.getSubmitDateTime() } returns now
        every { projection.getSessionTitle() } returns "테스트 세션"
        every { projection.getSessionStartDateTime() } returns now.plusDays(1).toLocalDate()

        every { absenceReportRepository.findMyAbsenceReports(member) } returns listOf(projection)

        // when
        val result = absenceService.getMyAbsenceReports(member)

        // then
        assertEquals(1, result.size)
        val dto = result[0]
        assertEquals(10L, dto.absenceReportId)
        assertEquals(AbsenceSubmitType.LATE, dto.absenceType)
        assertEquals(AbsenceReportApproval.APPROVED, dto.absenceReportApproval)
        assertEquals("테스트 세션", dto.sessionTitle)
    }


    // getById 성공 케이스
    @Test
    fun `getById - 존재하면 엔티티를 리턴한다`() {
        val report = mockk<AbsenceReport>()
        every { absenceReportRepository.findById(1L) } returns java.util.Optional.of(report)

        val result = absenceService.getById(1L)

        assertSame(report, result)
    }


    // getById 실패 케이스
    @Test
    fun `getById - 없으면 CustomException을 던진다`() {
        every { absenceReportRepository.findById(999L) } returns java.util.Optional.empty()

        val ex = assertThrows<CustomException> {
            absenceService.getById(999L)
        }
        assertEquals(AbsenceReportErrorCode.ABSENCE_REPORT_NOT_FOUND, ex.errorCode)
    }
}