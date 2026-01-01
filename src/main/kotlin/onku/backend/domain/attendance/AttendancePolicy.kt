package onku.backend.domain.attendance

object AttendancePolicy {
    // 토큰/세션 운영 정책
    const val TOKEN_TTL_SECONDS: Long = 15L       // 출석 토큰 TTL
    const val OPEN_GRACE_MINUTES: Long = 30L      // 세션 오픈 허용 범위(+30분)

    // 지각/결석 정책
    const val LATE_WINDOW_MINUTES: Long = 20L     // 지각 허용 20분
    const val SAFETY_OFFSET_MINUTES: Long = 1L

    // 결석 시작 경계 (= 21분)
    val ABSENT_START_MINUTES: Long
        get() = LATE_WINDOW_MINUTES + SAFETY_OFFSET_MINUTES
}
