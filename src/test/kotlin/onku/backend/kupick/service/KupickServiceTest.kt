package onku.backend.kupick.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.dto.KupickMemberInfo
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.kupick.repository.projection.KupickUrls
import onku.backend.domain.kupick.repository.projection.KupickWithProfile
import onku.backend.domain.kupick.service.KupickService
import onku.backend.domain.member.Member
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.verify
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class KupickServiceTest {

    @MockK
    lateinit var kupickRepository: KupickRepository

    lateinit var kupickService: KupickService

    @BeforeEach
    fun setUp() {
        kupickService = KupickService(kupickRepository)
    }

    private fun createMember(id: Long = 1L): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        return m
    }

    // ==========================
    // submitApplication
    // ==========================

    @Test
    fun `submitApplication - 기존 신청이 없으면 새로 생성하고 null 반환`() {
        val member = createMember(1L)
        val newUrl = "s3://kupick/apply/new.png"

        every {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(
                member,
                any(), // startOfMonth
                any()  // startOfNextMonth
            )
        } returns null

        every {
            kupickRepository.save(any())
        } returns mockk(relaxed = true)

        val result = kupickService.submitApplication(member, newUrl)

        assertNull(result)

        verify(exactly = 1) {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(member, any(), any())
        }
        verify(exactly = 1) {
            kupickRepository.save(any())
        }
    }

    @Test
    fun `submitApplication - 기존 신청 있고 미승인이면 기존 URL 반환하고 updateApplication 호출`() {
        val member = createMember(1L)
        val oldUrl = "s3://kupick/apply/old.png"
        val newUrl = "s3://kupick/apply/new.png"

        val existing = mockk<Kupick>(relaxed = true)
        every { existing.approval } returns false
        every { existing.applicationImageUrl } returns oldUrl
        every { existing.updateApplication(newUrl, any()) } just Runs

        every {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(member, any(), any())
        } returns existing

        val result = kupickService.submitApplication(member, newUrl)

        assertEquals(oldUrl, result)

        verify(exactly = 1) {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(member, any(), any())
        }
        verify(exactly = 1) {
            existing.updateApplication(newUrl, any())
        }
        verify(exactly = 0) { kupickRepository.save(any()) }
    }

    @Test
    fun `submitApplication - 기존 신청이 승인 상태면 KUPICK_NOT_UPDATE 예외`() {
        val member = createMember(1L)
        val newUrl = "s3://kupick/apply/new.png"

        val existing = mockk<Kupick>(relaxed = true)
        every { existing.approval } returns true

        every {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(member, any(), any())
        } returns existing

        val ex = assertFailsWith<CustomException> {
            kupickService.submitApplication(member, newUrl)
        }

        assertEquals(KupickErrorCode.KUPICK_NOT_UPDATE, ex.errorCode)

        verify(exactly = 1) {
            kupickRepository.findFirstByMemberAndApplicationDateBetween(member, any(), any())
        }
        verify(exactly = 0) { kupickRepository.save(any()) }
    }

    // ==========================
    // submitView
    // ==========================

    @Test
    fun `submitView - 이번달 신청이 없으면 KUPICK_APPLICATION_FIRST 예외`() {
        val member = createMember(1L)
        val viewUrl = "s3://kupick/view/new.png"

        every {
            kupickRepository.findThisMonthByMember(member, any(), any())
        } returns null

        val ex = assertFailsWith<CustomException> {
            kupickService.submitView(member, viewUrl)
        }

        assertEquals(KupickErrorCode.KUPICK_APPLICATION_FIRST, ex.errorCode)
        verify { kupickRepository.findThisMonthByMember(member, any(), any()) }
    }

    @Test
    fun `submitView - 이미 승인된 큐픽이면 KUPICK_NOT_UPDATE 예외`() {
        val member = createMember(1L)
        val viewUrl = "s3://kupick/view/new.png"

        val existing = mockk<Kupick>(relaxed = true)
        every { existing.approval } returns true

        every {
            kupickRepository.findThisMonthByMember(member, any(), any())
        } returns existing

        val ex = assertFailsWith<CustomException> {
            kupickService.submitView(member, viewUrl)
        }

        assertEquals(KupickErrorCode.KUPICK_NOT_UPDATE, ex.errorCode)

        verify { kupickRepository.findThisMonthByMember(member, any(), any()) }
        verify(exactly = 0) { existing.submitView(any(), any()) }
    }

    @Test
    fun `submitView - 정상 제출이면 이전 viewUrl 반환하고 submitView 호출`() {
        val member = createMember(1L)
        val oldViewUrl = "s3://kupick/view/old.png"
        val newViewUrl = "s3://kupick/view/new.png"

        val existing = mockk<Kupick>(relaxed = true)
        every { existing.approval } returns false
        every { existing.viewImageUrl } returns oldViewUrl
        every { existing.submitView(newViewUrl, any()) } just Runs

        every {
            kupickRepository.findThisMonthByMember(member, any(), any())
        } returns existing

        val result = kupickService.submitView(member, newViewUrl)

        assertEquals(oldViewUrl, result)

        verify { kupickRepository.findThisMonthByMember(member, any(), any()) }
        verify { existing.submitView(newViewUrl, any()) }
    }

    // ==========================
    // viewMyKupick
    // ==========================

    @Test
    fun `viewMyKupick - 이번달 조회 결과가 있으면 그대로 반환`() {
        val member = createMember(1L)
        val urls = mockk<KupickUrls>()

        every {
            kupickRepository.findUrlsForMemberInMonth(member, any(), any())
        } returns urls

        val result = kupickService.viewMyKupick(member)

        assertSame(urls, result)
        verify {
            kupickRepository.findUrlsForMemberInMonth(member, any(), any())
        }
    }

    @Test
    fun `viewMyKupick - 이번달 조회 결과가 없으면 null`() {
        val member = createMember(1L)

        every {
            kupickRepository.findUrlsForMemberInMonth(member, any(), any())
        } returns null

        val result = kupickService.viewMyKupick(member)

        assertNull(result)
        verify {
            kupickRepository.findUrlsForMemberInMonth(member, any(), any())
        }
    }

    // ==========================
    // findAllAsShowUpdateResponse
    // ==========================

    @Test
    fun `findAllAsShowUpdateResponse - year, month에 대한 결과 리스트 그대로 반환`() {
        val year = 2025
        val month = 9

        val row1 = mockk<KupickWithProfile>()
        val row2 = mockk<KupickWithProfile>()

        every {
            kupickRepository.findAllWithProfile(any(), any())
        } returns listOf(row1, row2)

        val result = kupickService.findAllAsShowUpdateResponse(year, month)

        assertEquals(2, result.size)
        assertSame(row1, result[0])
        assertSame(row2, result[1])

        verify { kupickRepository.findAllWithProfile(any(), any()) }
    }

    // ==========================
    // decideApproval
    // ==========================

    @Test
    fun `decideApproval - 존재하지 않으면 KUPICK_NOT_FOUND 예외`() {
        every { kupickRepository.findById(1L) } returns java.util.Optional.empty()

        val ex = assertFailsWith<CustomException> {
            kupickService.decideApproval(1L, true)
        }

        assertEquals(KupickErrorCode.KUPICK_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `decideApproval - 존재하면 updateApproval 호출`() {
        val kupick = mockk<Kupick>(relaxed = true)
        every { kupickRepository.findById(1L) } returns java.util.Optional.of(kupick)
        every { kupick.updateApproval(true) } just Runs

        kupickService.decideApproval(1L, true)

        verify { kupickRepository.findById(1L) }
        verify { kupick.updateApproval(true) }
    }

    // ==========================
    // findFcmInfo
    // ==========================

    @Test
    fun `findFcmInfo - 레포에서 조회한 KupickMemberInfo를 그대로 반환`() {
        val info = mockk<KupickMemberInfo>()

        every { kupickRepository.findFcmInfoByKupickId(10L) } returns info

        val result = kupickService.findFcmInfo(10L)

        assertSame(info, result)
        verify { kupickRepository.findFcmInfoByKupickId(10L) }
    }
}