package onku.backend.domain.attendance.dto

import onku.backend.domain.attendance.enums.AttendanceAvailabilityReason

data class AttendanceAvailabilityResponse(
    val available: Boolean,                         // 출석 가능 여부
    val reason: AttendanceAvailabilityReason?,    // 불가 사유 (가능하면 null)
)