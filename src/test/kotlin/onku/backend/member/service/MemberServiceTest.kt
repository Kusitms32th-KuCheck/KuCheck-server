package onku.backend.member.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.dto.StaffUpdateRequest
import onku.backend.domain.member.dto.UpdateRoleRequest
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.member.service.MemberService
import onku.backend.global.auth.jwt.JwtUtil
import onku.backend.global.exception.CustomException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*
import kotlin.test.Test
import java.time.Duration

@ExtendWith(MockKExtension::class)
class MemberServiceTest {

    @MockK lateinit var memberRepository: MemberRepository
    @MockK lateinit var memberProfileRepository: MemberProfileRepository
    @MockK lateinit var passwordEncoder: PasswordEncoder
    @MockK lateinit var jwtUtil: JwtUtil

    lateinit var service: MemberService
    private val onboardingTtl: Duration = Duration.ofDays(7)

    @BeforeEach
    fun setUp() {
        service = MemberService(
            memberRepository = memberRepository,
            memberProfileRepository = memberProfileRepository,
            passwordEncoder = passwordEncoder,
            jwtUtil = jwtUtil,
            onboardingTtl = onboardingTtl
        )
    }

    private fun createMember(
        id: Long = 1L,
        email: String? = "test@aaa.com",
        role: Role = Role.USER,
        socialType: SocialType = SocialType.KAKAO,
        socialId: String = "KAKAO",
        hasInfo: Boolean = false,
        approval: ApprovalStatus = ApprovalStatus.PENDING,
        isStaff: Boolean = false
    ): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        every { m.email } returns email
        every { m.role } returns role
        every { m.socialType } returns socialType
        every { m.socialId } returns socialId
        every { m.hasInfo } returns hasInfo
        every { m.approval } returns approval
        every { m.isStaff } returns isStaff

        // mutable property 는 set 도 열어줘야 할 때 있음
        every { m.role = any() } just Runs
        every { m.isStaff = any() } just Runs

        return m
    }

    // getByEmail
    @Test
    fun `getByEmail - 존재하는 이메일이면 Member 리턴`() {
        val member = createMember(id = 1L, email = "user@test.com")

        every { memberRepository.findByEmail("user@test.com") } returns member

        val result = service.getByEmail("user@test.com")

        assertEquals(1L, result.id)
        assertEquals("user@test.com", result.email)
    }

    @Test
    fun `getByEmail - 없으면 예외 발생`() {
        every { memberRepository.findByEmail("no@test.com") } returns null

        val ex = assertThrows(CustomException::class.java) {
            service.getByEmail("no@test.com")
        }
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }

    // upsertSocialMember
    @Test
    fun `upsertSocialMember - 기존 소셜 계정이 있으면 업데이트 후 그대로 리턴`() {
        val existing = createMember(
            id = 1L,
            email = "old@test.com",
            socialId = "KAKAO",
            socialType = SocialType.KAKAO
        )

        every { memberRepository.findBySocialIdAndSocialType("KAKAO", SocialType.KAKAO) } returns existing
        every { existing.updateEmail("new@test.com") } just Runs

        val result = service.upsertSocialMember(
            email = "new@test.com",
            socialId = "KAKAO",
            type = SocialType.KAKAO
        )

        assertSame(existing, result)
        verify(exactly = 0) { memberRepository.save(any<Member>()) }
        verify(exactly = 1) { existing.updateEmail("new@test.com") }
    }

    @Test
    fun `upsertSocialMember - 기존 소셜 계정이 없으면 새로 생성`() {
        every { memberRepository.findBySocialIdAndSocialType("KAKAO", SocialType.KAKAO) } returns null
        every { memberRepository.findByEmail("user@test.com") } returns null
        val slotMember = slot<Member>()
        every { memberRepository.save(capture(slotMember)) } answers { slotMember.captured }

        val result = service.upsertSocialMember(
            email = "user@test.com",
            socialId = "KAKAO",
            type = SocialType.KAKAO
        )

        // save 에 들어간 Member 검증
        assertEquals("user@test.com", slotMember.captured.email)
        assertEquals(Role.USER, slotMember.captured.role)
        assertEquals(SocialType.KAKAO, slotMember.captured.socialType)
        assertEquals("KAKAO", slotMember.captured.socialId)
        assertFalse(slotMember.captured.hasInfo)
        assertEquals(ApprovalStatus.PENDING, slotMember.captured.approval)

        assertSame(slotMember.captured, result)
    }

    // markOnboarded
    @Test
    fun `markOnboarded - 존재하지 않으면 예외`() {
        val member = createMember(id = 1L)

        every { memberRepository.findById(1L) } returns Optional.empty()

        val ex = assertThrows(CustomException::class.java) {
            service.markOnboarded(member)
        }
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `markOnboarded - hasInfo false면 onboarded 호출하고 save`() {
        val m = createMember(
            id = 1L,
            hasInfo = false
        )

        every { memberRepository.findById(1L) } returns Optional.of(m)
        every { m.onboarded() } just Runs
        every { memberRepository.save(m) } returns m

        service.markOnboarded(m)

        verify { m.onboarded() }
        verify { memberRepository.save(m) }
    }

    @Test
    fun `markOnboarded - 이미 hasInfo true면 아무것도 안 함`() {
        val m = createMember(
            id = 1L,
            hasInfo = true
        )

        every { memberRepository.findById(1L) } returns Optional.of(m)

        service.markOnboarded(m)

        // onboarded, save 호출 안 됨
        verify(exactly = 0) { m.onboarded() }
        verify(exactly = 0) { memberRepository.save(any<Member>()) }
    }

    // deleteMemberById
    @Test
    fun `deleteMemberById - 존재하지 않으면 예외`() {
        every { memberRepository.existsById(1L) } returns false

        val ex = assertThrows(CustomException::class.java) {
            service.deleteMemberById(1L)
        }
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }

    @Test
    fun `deleteMemberById - 프로필 있으면 프로필 먼저 삭제 후 멤버 삭제`() {
        every { memberRepository.existsById(1L) } returns true
        every { memberProfileRepository.existsByMember_Id(1L) } returns true
        every { memberProfileRepository.deleteByMemberId(1L) } returns 1
        every { memberRepository.deleteById(1L) } just Runs

        service.deleteMemberById(1L)

        verifyOrder {
            memberProfileRepository.existsByMember_Id(1L)
            memberProfileRepository.deleteByMemberId(1L)
            memberRepository.deleteById(1L)
        }
    }

    @Test
    fun `deleteMemberById - 프로필 없으면 멤버만 삭제`() {
        every { memberRepository.existsById(1L) } returns true
        every { memberProfileRepository.existsByMember_Id(1L) } returns false
        every { memberRepository.deleteById(1L) } just Runs

        service.deleteMemberById(1L)

        verify(exactly = 0) { memberProfileRepository.deleteByMemberId(any()) }
        verify(exactly = 1) { memberRepository.deleteById(1L) }
    }

    // updateRole
    @Test
    fun `updateRole - role이 null이면 예외`() {
        val req = UpdateRoleRequest(role = null)

        val ex = assertThrows(CustomException::class.java) {
            service.updateRole(1L, req)
        }
        assertEquals(MemberErrorCode.INVALID_REQUEST, ex.errorCode)
    }

    @Test
    fun `updateRole - 정상적으로 역할 변경`() {
        val member = createMember(id = 1L, role = Role.USER)

        every { memberRepository.findByIdOrNull(1L) } returns member
        every { memberRepository.save(member) } returns member

        val req = UpdateRoleRequest(role = Role.MANAGEMENT)

        service.updateRole(1L, req)

        verify { member.role = Role.MANAGEMENT }
        verify { memberRepository.save(member) }
    }

    // updateStaffMembers
    @Test
    fun `updateStaffMembers - 기존 운영진과 비교해 추가와 삭제 목록을 계산한다`() {
        val staff1 = createMember(id = 1L, isStaff = true, role = Role.STAFF)
        val staff2 = createMember(id = 2L, isStaff = true, role = Role.STAFF)

        // 현재 운영진: 1, 2
        every { memberRepository.findByIsStaffTrue() } returns listOf(staff1, staff2)

        // 요청: 2, 3 이 운영진이 되어야 함
        val req = StaffUpdateRequest(
            staffMemberIds = listOf(2L, 3L)
        )

        // 추가 대상으로 3L 멤버가 있다고 가정
        val newStaff = createMember(id = 3L, isStaff = false, role = Role.USER)
        every { memberRepository.findByIdIn(setOf(3L)) } returns listOf(newStaff)

        val result = service.updateStaffMembers(req)

        // 추가된 운영진: 3, 제거된 운영진: 1
        assertEquals(listOf(3L), result.addedStaffs)
        assertEquals(listOf(1L), result.removedStaffs)

        // 3은 isStaff true, role STAFF 로 바뀌어야 함
        verify { newStaff.isStaff = true }
        verify { newStaff.role = Role.STAFF }

        // 1은 isStaff false, role USER 로 바뀌어야 함
        verify { staff1.isStaff = false }
        verify { staff1.role = Role.USER }

        // 2는 그대로 staff 유지 (바꾸지 않아도 됨)
        verify(exactly = 0) { staff2.isStaff = false }
    }
}