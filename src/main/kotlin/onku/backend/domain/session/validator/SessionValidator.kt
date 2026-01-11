package onku.backend.domain.session.validator

import onku.backend.domain.session.Session
import onku.backend.domain.session.enums.SessionCategory
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Component
class SessionValidator {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    /** 지난 세션 여부 */
    fun isPastSession(session: Session, now: LocalDateTime = LocalDateTime.now(zone)): Boolean {
        return session.startDate.isBefore(now.toLocalDate())
    }

    /** 금/토에 바로 앞 토요일 세션인지 여부 */
    fun isImminentSession(session: Session, now: LocalDateTime = LocalDateTime.now(zone)): Boolean {
        val sessionDate = session.startDate  // 이미 LocalDate 타입
        val today = now.toLocalDate()

        // 세션이 속한 주의 목요일 계산
        val sessionThursday = sessionDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.THURSDAY))

        // 목요일 00:00부터는 불가 → 목요일보다 이전일 때만 true
        return today.isBefore(sessionThursday)
    }

    /** 휴회 세션인지 여부 */
    fun isRestSession(session: Session) : Boolean{
        return (session.category == SessionCategory.REST)
    }
}