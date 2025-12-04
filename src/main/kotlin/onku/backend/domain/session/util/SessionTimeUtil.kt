package onku.backend.domain.session.util

import onku.backend.domain.session.Session
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import java.time.LocalDateTime
import java.time.LocalTime

object SessionTimeUtil {

    /**
     * Session.startDate(LocalDate) + SessionDetail.startTime(LocalTime) => LocalDateTime
     */
    @JvmStatic
    fun startDateTime(session: Session): LocalDateTime {
        val date = session.startDate
        val time: LocalTime = session.sessionDetail?.startTime
            ?: throw CustomException(ErrorCode.INVALID_REQUEST)
        return LocalDateTime.of(date, time)
    }

    /**
     * 결석 판정 경계 시각 = startDateTime + absentStartMinutes
     */
    @JvmStatic
    fun absentBoundary(session: Session, absentStartMinutes: Long): LocalDateTime =
        startDateTime(session).plusMinutes(absentStartMinutes)
}
