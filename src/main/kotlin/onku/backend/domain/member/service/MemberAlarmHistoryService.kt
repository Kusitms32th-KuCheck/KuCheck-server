package onku.backend.domain.member.service

import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberAlarmHistory
import onku.backend.domain.member.dto.MemberAlarmHistoryItemResponse
import onku.backend.domain.member.repository.MemberAlarmHistoryRepository
import onku.backend.global.alarm.enums.AlarmEmojiType
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

    @Transactional
    fun saveAlarm(member: Member, alarmEmojiType: AlarmEmojiType, message : String) {
        memberAlarmHistoryRepository.save(
            MemberAlarmHistory(
                member = member,
                message = message,
                type = alarmEmojiType
            )
        )
    }
}
