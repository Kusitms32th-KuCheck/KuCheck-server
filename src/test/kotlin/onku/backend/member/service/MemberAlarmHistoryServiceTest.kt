package onku.backend.member.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import onku.backend.domain.member.Member
import onku.backend.domain.member.MemberAlarmHistory
import onku.backend.domain.member.dto.MemberAlarmHistoryItemResponse
import onku.backend.domain.member.repository.MemberAlarmHistoryRepository
import onku.backend.domain.member.service.MemberAlarmHistoryService
import onku.backend.global.alarm.enums.AlarmEmojiType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class MemberAlarmHistoryServiceTest {

    @MockK lateinit var memberAlarmHistoryRepository: MemberAlarmHistoryRepository

    lateinit var service: MemberAlarmHistoryService

    @BeforeEach
    fun setUp() {
        service = MemberAlarmHistoryService(memberAlarmHistoryRepository)
    }

    private fun createMember(id: Long = 1L): Member {
        val m = mockk<Member>(relaxed = true)
        every { m.id } returns id
        return m
    }

    // getMyAlarms 테스트
    @Test
    fun `getMyAlarms - 히스토리를 최신순으로 DTO로 변환해서 반환한다`() {
        // given
        val member = createMember(1L)

        val createdAt1 = LocalDateTime.of(2025, 1, 1, 10, 0)
        val createdAt2 = LocalDateTime.of(2025, 1, 2, 9, 30)

        // 엔티티는 mock으로 충분 (필드 몇 개만 쓰니까)
        val history1 = mockk<MemberAlarmHistory>()
        every { history1.message } returns "첫 번째 알림"
        every { history1.type } returns AlarmEmojiType.STAR
        every { history1.createdAt } returns createdAt1

        val history2 = mockk<MemberAlarmHistory>()
        every { history2.message } returns "두 번째 알림"
        every { history2.type } returns AlarmEmojiType.WARNING
        every { history2.createdAt } returns createdAt2

        every {
            memberAlarmHistoryRepository.findByMemberOrderByCreatedAtDesc(member)
        } returns listOf(history2, history1)

        // when
        val result: List<MemberAlarmHistoryItemResponse> = service.getMyAlarms(member)

        // then
        assertEquals(2, result.size)

        assertEquals("두 번째 알림", result[0].message)
        assertEquals(AlarmEmojiType.WARNING, result[0].type)
        assertEquals("01/02 09:30", result[0].createdAt)

        assertEquals("첫 번째 알림", result[1].message)
        assertEquals(AlarmEmojiType.STAR, result[1].type)
        assertEquals("01/01 10:00", result[1].createdAt)

        verify(exactly = 1) {
            memberAlarmHistoryRepository.findByMemberOrderByCreatedAtDesc(member)
        }
    }

    // saveAlarm 테스트
    @Test
    fun `saveAlarm - member와 타입, 메시지로 알림 히스토리를 저장한다`() {
        // given
        val member = createMember(1L)
        val type = AlarmEmojiType.STAR
        val message = "쿠픽 승인 되었습니다."

        val slot = slot<MemberAlarmHistory>()
        every { memberAlarmHistoryRepository.save(capture(slot)) } answers {
            slot.captured
        }

        // when
        service.saveAlarm(member, type, message)

        // then
        verify(exactly = 1) {
            memberAlarmHistoryRepository.save(any())
        }

        val saved = slot.captured
        assertEquals(member, saved.member)
        assertEquals(message, saved.message)
        assertEquals(type, saved.type)
    }
}