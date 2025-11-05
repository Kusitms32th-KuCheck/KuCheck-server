package onku.backend.domain.point.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.converter.MemberPointConverter
import onku.backend.domain.point.dto.MemberPointHistoryResponse
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.global.exception.CustomException
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberPointHistoryService(
    private val recordRepository: MemberPointHistoryRepository,
    private val memberProfileRepository: MemberProfileRepository
) {

    @Transactional(readOnly = true)
    fun getHistory(member: Member, safePage: Int, size: Int): MemberPointHistoryResponse {
        val pageable = PageRequest.of(safePage, size)

        val profile = memberProfileRepository.findById(member.id!!)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }
        val name = profile.name

        // 누적 합계
        val sums = recordRepository.sumPointsForMember(member)
        val plusPoints = sums.getPlusPoints().toInt()
        val minusPoints = sums.getMinusPoints().toInt()
        val totalPoints = sums.getTotalPoints().toInt()

        // 페이지 목록 (최신순)
        val page = recordRepository.findByMemberOrderByOccurredAtDesc(member, pageable)
        val records = page.content.map(MemberPointConverter::toResponse)

        return MemberPointHistoryResponse(
            memberId = member.id!!,
            name = name,
            plusPoints = plusPoints,
            minusPoints = minusPoints,
            totalPoints = totalPoints,
            records = records,
            totalPages = page.totalPages,
            isLastPage = page.isLast
        )
    }
}
