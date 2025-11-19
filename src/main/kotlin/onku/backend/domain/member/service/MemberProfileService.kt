package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.dto.*
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.page.PageResponse
import onku.backend.global.s3.dto.GetUpdateAndDeleteUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberProfileService(
    private val memberProfileRepository: MemberProfileRepository,
    private val memberRepository: MemberRepository,
    private val memberService: MemberService,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    private val s3Service: S3Service
) {
    fun submitOnboarding(member: Member, req: OnboardingRequest): OnboardingResponse {
        if (member.hasInfo) { // 이미 온보딩 완료된 사용자 차단
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }
        if (member.approval != ApprovalStatus.PENDING) { // PENDING 상태가 아닌 사용자 차단
            throw CustomException(MemberErrorCode.INVALID_MEMBER_STATE)
        }

        // FCM 토큰 저장
        val m = memberRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }
        m.updateFcmToken(req.fcmToken)

        createOrUpdateProfile(m.id!!, req)
        memberService.markOnboarded(m)

        return OnboardingResponse(
            status = ApprovalStatus.PENDING
        )
    }

    private fun createOrUpdateProfile(memberId: Long, req: OnboardingRequest) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val existing = memberProfileRepository.findById(memberId).orElse(null)
        if (existing == null) {
            val profile = MemberProfile(
                member = member,
                name = req.name,
                school = req.school,
                major = req.major,
                part = req.part,
                phoneNumber = req.phoneNumber,
                profileImage = null
            )
            memberProfileRepository.save(profile)
        } else {
            existing.apply(
                name = req.name,
                school = req.school,
                major = req.major,
                part = req.part,
                phoneNumber = req.phoneNumber
            )
        }
    }

    @Transactional(readOnly = true)
    fun getProfileSummary(member: Member): MemberProfileResponse {
        val profile = memberProfileRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val sums = memberPointHistoryRepository.sumPointsForMember(member)
        val total = sums.getTotalPoints()

        val key = profile.profileImage
        val url = key?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }

        return MemberProfileResponse(
            name = profile.name,
            part = profile.part,
            totalPoints = total,
            profileImage = url
        )
    }

    @Transactional(readOnly = true)
    fun getProfileBasics(member: Member): MemberProfileBasicsResponse {
        val profile = memberProfileRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val key = profile.profileImage
        val url = key?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }

        return MemberProfileBasicsResponse(
            name = profile.name ?: "Unknown",
            part = profile.part,
            school = profile.school,
            profileImageUrl = url
        )
    }

    @Transactional
    fun issueProfileImageUploadUrl(member: Member, fileName: String): GetUpdateAndDeleteUrlDto {
        val signed = s3Service.getPostS3Url(
            memberId = member.id!!,
            filename = fileName,
            folderName = FolderName.MEMBER_PROFILE.name,
            option = UploadOption.IMAGE
        )

        val oldKey = submitProfileImage(member, signed.key)

        val oldDeletePreSignedUrl = if (!oldKey.isNullOrBlank()) {
            s3Service.getDeleteS3Url(oldKey).preSignedUrl
        } else {
            ""
        }

        return GetUpdateAndDeleteUrlDto(
            newUrl = signed.preSignedUrl,
            oldUrl = oldDeletePreSignedUrl
        )
    }

    fun submitProfileImage(member: Member, newKey: String): String? {
        val profile = memberProfileRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val old = profile.profileImage
        profile.updateProfileImage(newKey)
        return old
    }

    fun updateProfile(
        memberId: Long,
        req: MemberProfileUpdateRequest
    ): MemberProfileBasicsResponse {
        memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val profile = memberProfileRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        profile.apply(
            name = req.name,
            school = req.school,
            major = req.major,
            part = req.part,
            phoneNumber = req.phoneNumber
        )

        val key = profile.profileImage
        val url = key?.let { s3Service.getGetS3Url(memberId, it).preSignedUrl }

        return MemberProfileBasicsResponse(
            name = profile.name ?: "Unknown",
            part = profile.part,
            school = profile.school,
            profileImageUrl = url
        )
    }

    @Transactional(readOnly = true)
    fun getApprovedMembersPagedWithCounts(
        page: Int,
        size: Int,
        isStaff: Boolean?
    ): MembersPagedResponse {

        val pendingCount = memberRepository.countByApproval(ApprovalStatus.PENDING)
        val approvedCount = memberRepository.countByApproval(ApprovalStatus.APPROVED)
        val rejectedCount = memberRepository.countByApproval(ApprovalStatus.REJECTED)

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.asc("part"),
                Sort.Order.asc("name")
            )
        )

        val profilePage = when (isStaff) {
            null -> memberProfileRepository.findByMemberApproval(
                ApprovalStatus.APPROVED,
                pageable
            )
            else -> memberProfileRepository.findByMemberApprovalAndMemberIsStaff(
                ApprovalStatus.APPROVED,
                isStaff,
                pageable
            )
        }
        val dtoPage = profilePage.map { profile ->
            val member = profile.member
            val key = profile.profileImage
            val url = key?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }

            MemberItemResponse(
                memberId = member.id!!,
                name = profile.name,
                profileImageUrl = url,
                part = profile.part,
                school = profile.school,
                major = profile.major,
                phoneNumber = profile.phoneNumber,
                socialType = member.socialType,
                email = member.email,
                role = member.role,
                isStaff = member.isStaff,
                approval = member.approval
            )
        }
        val pageResponse = PageResponse.from(dtoPage)
        return MembersPagedResponse(
            pendingCount = pendingCount,
            approvedCount = approvedCount,
            rejectedCount = rejectedCount,
            members = pageResponse
        )
    }

    @Transactional(readOnly = true)
    fun getApprovalRequestMembers(page: Int, size: Int): MembersPagedResponse {
        val pendingCount = memberRepository.countByApproval(ApprovalStatus.PENDING)
        val approvedCount = memberRepository.countByApproval(ApprovalStatus.APPROVED)
        val rejectedCount = memberRepository.countByApproval(ApprovalStatus.REJECTED)

        val pageable = PageRequest.of(page, size)

        val approvalStatuses = listOf(ApprovalStatus.PENDING, ApprovalStatus.REJECTED)
        val profilePage = memberProfileRepository.findByMemberApprovalIn(approvalStatuses, pageable)

        val memberPage = profilePage.map { profile ->
            val member = profile.member
            val key = profile.profileImage
            val url = key?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }

            MemberItemResponse(
                memberId = member.id!!,
                name = profile.name,
                profileImageUrl = url,
                part = profile.part,
                school = profile.school,
                major = profile.major,
                phoneNumber = profile.phoneNumber,
                socialType = member.socialType,
                email = member.email,
                role = member.role,
                isStaff = member.isStaff,
                approval = member.approval
            )
        }
        val membersPageResponse: PageResponse<MemberItemResponse> = PageResponse.from(memberPage)

        return MembersPagedResponse(
            pendingCount = pendingCount,
            approvedCount = approvedCount,
            rejectedCount = rejectedCount,
            members = membersPageResponse
        )
    }
}