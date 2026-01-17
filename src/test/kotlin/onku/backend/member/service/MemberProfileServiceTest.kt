package onku.backend.member.service

import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.*
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.dto.MemberProfileBasicsResponse
import onku.backend.domain.member.dto.MemberProfileUpdateRequest
import onku.backend.domain.member.dto.MembersPagedResponse
import onku.backend.domain.member.dto.OnboardingRequest
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.member.service.MemberProfileService
import onku.backend.domain.member.service.MemberService
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.s3.dto.GetS3UrlDto
import onku.backend.global.s3.dto.GetUpdateAndDeleteUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*
import kotlin.test.Test
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class MemberProfileServiceTest {

    @MockK lateinit var memberProfileRepository: MemberProfileRepository
    @MockK lateinit var memberRepository: MemberRepository
    @MockK lateinit var memberService: MemberService
    @MockK lateinit var memberPointHistoryRepository: MemberPointHistoryRepository
    @MockK lateinit var s3Service: S3Service

    lateinit var service: MemberProfileService

    @BeforeEach
    fun setUp() {
        service = MemberProfileService(
            memberProfileRepository = memberProfileRepository,
            memberRepository = memberRepository,
            memberService = memberService,
            memberPointHistoryRepository = memberPointHistoryRepository,
            s3Service = s3Service
        )
    }

    private fun createMember(
        id: Long = 1L,
        hasInfo: Boolean = false,
        approval: ApprovalStatus = ApprovalStatus.PENDING,
        isStaff: Boolean = false,
        role: Role = Role.GUEST,
        socialType: SocialType = SocialType.KAKAO
    ): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        every { m.hasInfo } returns hasInfo
        every { m.approval } returns approval
        every { m.isStaff } returns isStaff
        every { m.role } returns role
        every { m.socialType } returns socialType
        every { m.email } returns "user$id@test.com"
        every { m.updateFcmToken(any()) } just Runs
        return m
    }

    private fun createProfile(
        member: Member,
        name: String? = "홍길동",
        part: Part = Part.BACKEND,
        school: String? = "K대",
        major: String? = "컴공",
        phone: String? = "010-0000-0000",
        profileImage: String? = null
    ): MemberProfile {
        return MemberProfile(
            member = member,
            name = name,
            school = school,
            major = major,
            part = part,
            phoneNumber = phone,
            profileImage = profileImage
        )
    }

    // submitOnboarding

    @Test
    fun `submitOnboarding - PENDING + hasInfo false면 프로필 생성하고 온보딩 처리`() {
        val member = createMember(id = 1L, hasInfo = false, approval = ApprovalStatus.PENDING)

        val req = OnboardingRequest(
            name = "홍길동",
            school = "K대",
            major = "컴공",
            part = Part.BACKEND,
            phoneNumber = "010-0000-0000",
            fcmToken = "fcm-123"
        )

        val persistedMember = createMember(id = 1L, hasInfo = false, approval = ApprovalStatus.PENDING)
        every { memberRepository.findById(1L) } returns Optional.of(persistedMember)
        every { memberProfileRepository.findById(1L) } returns Optional.empty()
        every { memberProfileRepository.save(any<MemberProfile>()) } answers { firstArg() }
        every { memberService.markOnboarded(persistedMember) } just Runs

        val result = service.submitOnboarding(member, req)

        assertEquals(ApprovalStatus.PENDING, result.status)

        val slot = slot<MemberProfile>()

        every { memberProfileRepository.save(capture(slot)) } answers { slot.captured }

        service.submitOnboarding(member, req)

        // then
        val saved = slot.captured
        assertEquals("홍길동", saved.name)
        assertEquals("K대", saved.school)
        assertEquals("컴공", saved.major)
        assertEquals(Part.BACKEND, saved.part)
        assertEquals("010-0000-0000", saved.phoneNumber)
    }

    @Test
    fun `submitOnboarding - 이미 hasInfo true면 INVALID_MEMBER_STATE`() {
        val member = createMember(id = 1L, hasInfo = true, approval = ApprovalStatus.PENDING)

        val req = OnboardingRequest(
            name = "홍길동",
            school = "K대",
            major = "컴공",
            part = Part.BACKEND,
            phoneNumber = "010-0000-0000",
            fcmToken = "fcm-123"
        )

        val ex = assertFailsWith<CustomException> {
            service.submitOnboarding(member, req)
        }
        assertEquals(MemberErrorCode.INVALID_MEMBER_STATE, ex.errorCode)
    }

    @Test
    fun `submitOnboarding - approval이 PENDING이 아니면 INVALID_MEMBER_STATE`() {
        val member = createMember(id = 1L, hasInfo = false, approval = ApprovalStatus.APPROVED)

        val req = OnboardingRequest(
            name = "홍길동",
            school = "K대",
            major = "컴공",
            part = Part.BACKEND,
            phoneNumber = "010-0000-0000",
            fcmToken = "fcm-123"
        )

        val ex = assertFailsWith<CustomException> {
            service.submitOnboarding(member, req)
        }
        assertEquals(MemberErrorCode.INVALID_MEMBER_STATE, ex.errorCode)
    }

    // getProfileSummary

    @Test
    fun `getProfileSummary - 프로필과 포인트, 프로필 이미지 URL을 반환`() {
        val member = createMember(id = 1L)
        every { member.email } returns "user1@test.com"

        val profile = createProfile(
            member = member,
            name = "홍길동",
            part = Part.BACKEND,
            school = "K대",
            major = "컴공",
            phone = "010-0000-0000",
            profileImage = "PROFILE/1/a.png"
        )

        every { memberProfileRepository.findById(1L) } returns Optional.of(profile)

        // sumPointsForMember projection mock
        every {
            memberPointHistoryRepository.sumPointsForMember(member)
        } returns mockk {
            every { getTotalPoints() } returns 80L
        }

        every {
            s3Service.getGetS3Url(1L, "PROFILE/1/a.png")
        } returns GetS3UrlDto(
            key = "PROFILE/1/a.png",
            preSignedUrl = "https://example.com/profile1.png",
            originalName = "a.png"
        )

        val resp = service.getProfileSummary(member)

        assertEquals("user1@test.com", resp.email)
        assertEquals("홍길동", resp.name)
        assertEquals(Part.BACKEND, resp.part)
        assertEquals(80L, resp.totalPoints)
        assertEquals("https://example.com/profile1.png", resp.profileImage)
    }

    // issueProfileImageUploadUrl

    @Test
    fun `issueProfileImageUploadUrl - 새 업로드 URL과 기존 삭제 URL을 반환`() {
        val member = createMember(1L)

        // 기존 프로필: 이전 이미지 키 존재
        val profile = createProfile(
            member = member,
            profileImage = "PROFILE/1/old.png"
        )
        every { memberProfileRepository.findById(1L) } returns Optional.of(profile)

        // 새 업로드용 pre-signed POST URL
        every {
            s3Service.getPostS3Url(
                memberId = 1L,
                filename = "new.png",
                folderName = FolderName.MEMBER_PROFILE.name,
                option = UploadOption.IMAGE
            )
        } returns GetS3UrlDto(
            key = "PROFILE/1/new.png",
            preSignedUrl = "https://upload-url",
            originalName = "new.png"
        )

        // 기존 삭제용 URL
        every {
            s3Service.getDeleteS3Url("PROFILE/1/old.png")
        } returns GetS3UrlDto(
            key = "PROFILE/1/old.png",
            preSignedUrl = "https://delete-old",
            originalName = "old.png"
        )

        val result: GetUpdateAndDeleteUrlDto =
            service.issueProfileImageUploadUrl(member, "new.png")

        // newUrl, oldUrl 확인
        assertEquals("https://upload-url", result.newUrl)
        assertEquals("https://delete-old", result.oldUrl)

        // submitProfileImage 안에서 profileImage 키가 바뀌었는지
        assertEquals("PROFILE/1/new.png", profile.profileImage)

        verify {
            s3Service.getPostS3Url(1L, "new.png", FolderName.MEMBER_PROFILE.name, UploadOption.IMAGE)
            s3Service.getDeleteS3Url("PROFILE/1/old.png")
        }
    }
    // submitProfileImage
    @Test
    fun `submitProfileImage - old key를 반환하고 프로필 이미지 키를 바꾼다`() {
        val member = createMember(1L)
        val profile = createProfile(member = member, profileImage = "PROFILE/1/old.png")

        every { memberProfileRepository.findById(1L) } returns Optional.of(profile)

        val old = service.submitProfileImage(member, "PROFILE/1/new.png")

        assertEquals("PROFILE/1/old.png", old)
        assertEquals("PROFILE/1/new.png", profile.profileImage)
    }

    // updateProfile

    @Test
    fun `updateProfile - 기본 정보 수정 후 basics 응답 반환`() {
        val member = createMember(1L)
        val profile = createProfile(member = member, name = "기존이름", profileImage = "PROFILE/1/a.png")

        every { memberRepository.findById(1L) } returns Optional.of(member)
        every { memberProfileRepository.findById(1L) } returns Optional.of(profile)

        every {
            s3Service.getGetS3Url(1L, "PROFILE/1/a.png")
        } returns GetS3UrlDto(
            key = "PROFILE/1/a.png",
            preSignedUrl = "https://profile-url",
            originalName = "a.png"
        )

        val req = MemberProfileUpdateRequest(
            name = "새 이름",
            school = "새 학교",
            major = "새 전공",
            part = Part.FRONTEND,
            phoneNumber = "010-1111-2222"
        )

        val resp: MemberProfileBasicsResponse = service.updateProfile(1L, req)

        assertEquals("새 이름", resp.name)
        assertEquals(Part.FRONTEND, resp.part)
        assertEquals("새 학교", resp.school)
        assertEquals("https://profile-url", resp.profileImageUrl)

        assertEquals("새 이름", profile.name)
        assertEquals("새 학교", profile.school)
        assertEquals("새 전공", profile.major)
        assertEquals(Part.FRONTEND, profile.part)
        assertEquals("010-1111-2222", profile.phoneNumber)
    }

    // getApprovedMembersPagedWithCounts

    @Test
    fun `getApprovedMembersPagedWithCounts - isStaff null이면 전체 승인 멤버 조회`() {
        every { memberRepository.countByApproval(ApprovalStatus.PENDING) } returns 2L
        every { memberRepository.countByApproval(ApprovalStatus.APPROVED) } returns 10L
        every { memberRepository.countByApproval(ApprovalStatus.REJECTED) } returns 1L

        val member = createMember(
            id = 1L,
            isStaff = true,
            approval = ApprovalStatus.APPROVED,
            role = Role.USER,
            socialType = SocialType.KAKAO
        )

        val profile = createProfile(
            member = member,
            name = "홍길동",
            part = Part.BACKEND,
            school = "K대",
            major = "컴공",
            phone = "010-0000-0000",
            profileImage = "PROFILE/1/a.png"
        )

        every {
            memberProfileRepository.findByMemberApproval(
                ApprovalStatus.APPROVED,
                any()
            )
        } returns PageImpl(listOf(profile), PageRequest.of(0, 10), 1)

        every {
            s3Service.getGetS3Url(1L, "PROFILE/1/a.png")
        } returns GetS3UrlDto(
            key = "PROFILE/1/a.png",
            preSignedUrl = "https://profile-url",
            originalName = "a.png"
        )

        val resp: MembersPagedResponse =
            service.getApprovedMembersPagedWithCounts(page = 0, size = 10, isStaff = null)

        assertEquals(2L, resp.pendingCount)
        assertEquals(10L, resp.approvedCount)
        assertEquals(1L, resp.rejectedCount)

        assertEquals(1, resp.members.data.size)
        val item = resp.members.data[0]
        assertEquals(1L, item.memberId)
        assertEquals("홍길동", item.name)
        assertEquals(Part.BACKEND, item.part)
        assertEquals("K대", item.school)
        assertEquals("컴공", item.major)
        assertEquals("010-0000-0000", item.phoneNumber)
        assertTrue(item.isStaff)
        assertEquals(ApprovalStatus.APPROVED, item.approval)
        assertEquals("https://profile-url", item.profileImageUrl)
    }

    // getApprovalRequestMembers

    @Test
    fun `getApprovalRequestMembers - PENDING, REJECTED 상태 멤버 목록과 카운트 반환`() {
        every { memberRepository.countByApproval(ApprovalStatus.PENDING) } returns 3L
        every { memberRepository.countByApproval(ApprovalStatus.APPROVED) } returns 7L
        every { memberRepository.countByApproval(ApprovalStatus.REJECTED) } returns 2L

        val member = createMember(
            id = 1L,
            isStaff = false,
            approval = ApprovalStatus.PENDING
        )

        val profile = createProfile(
            member = member,
            name = "신청자",
            part = Part.BACKEND,
            school = "K대",
            major = "컴공",
            phone = "010-0000-1111",
            profileImage = "PROFILE/1/a.png"
        )

        every {
            memberProfileRepository.findByMember_ApprovalIn(
                listOf(ApprovalStatus.PENDING, ApprovalStatus.REJECTED),
                any()
            )
        } returns PageImpl(listOf(profile), PageRequest.of(0, 10), 1)

        every {
            s3Service.getGetS3Url(1L, "PROFILE/1/a.png")
        } returns GetS3UrlDto(
            key = "PROFILE/1/a.png",
            preSignedUrl = "https://profile-url",
            originalName = "a.png"
        )

        val resp: MembersPagedResponse =
            service.getApprovalRequestMembers(page = 0, size = 10)

        assertEquals(3L, resp.pendingCount)
        assertEquals(7L, resp.approvedCount)
        assertEquals(2L, resp.rejectedCount)

        assertEquals(1, resp.members.data.size)
        val item = resp.members.data[0]
        assertEquals(1L, item.memberId)
        assertEquals("신청자", item.name)
        assertEquals(ApprovalStatus.PENDING, item.approval)
        assertEquals("https://profile-url", item.profileImageUrl)
    }
}