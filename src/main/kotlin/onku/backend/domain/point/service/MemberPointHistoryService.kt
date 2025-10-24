package onku.backend.domain.point.service

import onku.backend.domain.member.Member
import onku.backend.domain.point.converter.MemberPointConverter
import onku.backend.domain.point.dto.MemberPointHistoryResponse
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.max

@Service
class MemberPointHistoryService(
    private val recordRepository: MemberPointHistoryRepository
) {

    @Transactional(readOnly = true)
    fun getHistory(member: Member, page1Based: Int, size: Int): MemberPointHistoryResponse {
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

        return MemberPointHistoryResponse(
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
