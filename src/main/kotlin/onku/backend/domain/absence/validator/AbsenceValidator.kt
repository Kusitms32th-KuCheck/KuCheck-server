package onku.backend.domain.absence.validator

import onku.backend.domain.session.Session
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Component
class AbsenceValidator {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    /** 지난 세션 여부 */
    fun isPastSession(session: Session, now: LocalDateTime = LocalDateTime.now(zone)): Boolean {
        return session.endTime.isBefore(now)
    }

    /** 금/토에 바로 앞 토요일 세션인지 여부 */
    fun isImminentSession(session: Session, now: LocalDateTime = LocalDateTime.now(zone)): Boolean {
        val today = now.toLocalDate()
        val isFriOrSat = today.dayOfWeek == DayOfWeek.FRIDAY || today.dayOfWeek == DayOfWeek.SATURDAY
        if (!isFriOrSat) return false

        val upcomingSaturday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        val sessionDate = session.startTime.atZone(zone).toLocalDate()
        return sessionDate == upcomingSaturday
    }
}