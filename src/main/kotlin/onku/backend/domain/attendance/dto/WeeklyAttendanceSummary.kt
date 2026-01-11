package onku.backend.domain.attendance.dto

data class WeeklyAttendanceSummary(
    val present: Long,     // 출석 = PRESENT_HOLIDAY + PRESENT
    val earlyLeave: Long,  // 조퇴 = EARLY_LEAVE
    val late: Long,        // 지각 = LATE
    val absent: Long       // 결석 = EXCUSED + ABSENT + ABSENT_WITH_DOC
) {
    val total: Long get() = present + earlyLeave + late + absent
}