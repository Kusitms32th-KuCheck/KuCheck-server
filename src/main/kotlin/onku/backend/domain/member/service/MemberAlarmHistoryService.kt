package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.dto.MemberAlarmHistoryItemResponse
import onku.backend.domain.member.repository.MemberAlarmHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
@Transactional(readOnly = true)
class MemberAlarmHistoryService(
    private val memberAlarmHistoryRepository: MemberAlarmHistoryRepository
) {

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd HH:mm")

    fun getMyAlarms(member: Member): List<MemberAlarmHistoryItemResponse> {
        val histories = memberAlarmHistoryRepository.findByMemberOrderByCreatedAtDesc(member)

        return histories.map {
            MemberAlarmHistoryItemResponse(
                message = it.message,
                type = it.type,
                createdAt = it.createdAt.format(formatter)
            )
        }
    }
}
