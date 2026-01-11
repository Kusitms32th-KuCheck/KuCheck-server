package onku.backend.domain.attendance.enums

enum class AttendanceAvailabilityReason {
    NO_OPEN_SESSION,       // 열린 세션이 없음
    ALREADY_RECORDED       // 해당 세션에 이미 출석 기록이 있음
}