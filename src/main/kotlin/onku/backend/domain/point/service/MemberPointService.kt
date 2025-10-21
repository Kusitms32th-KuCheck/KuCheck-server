package onku.backend.domain.point.service

import onku.backend.domain.member.Member
import onku.backend.domain.point.converter.MemberPointConverter
import onku.backend.domain.point.dto.UserPointHistoryResponse
import onku.backend.domain.point.repository.MemberPointRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max

@Service
class MemberPointService(
    private val recordRepository: MemberPointRepository
) {

    @Transactional(readOnly = true)
    fun getHistory(member: Member, page1Based: Int, size: Int): UserPointHistoryResponse {
        val safePage = max(0, page1Based - 1)
        val pageable = PageRequest.of(safePage, size)

        // 누적 합계
        val sums = recordRepository.sumPointsForMember(member)
        val plusPoints = sums.getPlusPoints().toInt()
        val minusPoints = sums.getMinusPoints().toInt()
        val totalPoints = sums.getTotalPoints().toInt()

        // 페이지 목록 (최신순)
        val page = recordRepository.findByMemberOrderByOccurredAtDesc(member, pageable)
        val records = page.content.map(MemberPointConverter::toResponse)

        return UserPointHistoryResponse(
            memberId = member.id!!,
            plusPoints = plusPoints,
            minusPoints = minusPoints,
            totalPoints = totalPoints,
            records = records,
            totalPages = page.totalPages,
            isLastPage = page.isLast
        )
    }
}
