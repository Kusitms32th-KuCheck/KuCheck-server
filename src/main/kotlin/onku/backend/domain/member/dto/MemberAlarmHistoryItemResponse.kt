package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.global.alarm.enums.AlarmEmojiType

@Schema(description = "내 알림 히스토리 한 건 응답")
data class MemberAlarmHistoryItemResponse(

    @Schema(description = "알림 메시지 내용")
    val message: String?,

    @Schema(description = "알림 이모지 타입")
    val type: AlarmEmojiType,

    @Schema(description = "알림 생성 시각, 포맷: MM/dd HH:mm", example = "11/19 13:45")
    val createdAt: String
)
