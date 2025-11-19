package onku.backend.global.alarm.enums

enum class AlarmType(
    val title: String,
) {
    KUPICK("큐픽 관련 알림입니다."),
    ABSENCE_REPORT("불참 사유서 관련 알림입니다."),
}