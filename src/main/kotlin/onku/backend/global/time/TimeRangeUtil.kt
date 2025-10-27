package onku.backend.global.time

import java.time.*
import java.time.temporal.TemporalAdjusters

object TimeRangeUtil {
    private val ZONE: ZoneId = ZoneId.of("Asia/Seoul")
    data class MonthRange(
        val startOfMonth: LocalDateTime,
        val startOfNextMonth: LocalDateTime
    )
    data class WeekRange(
        val startOfWeek: LocalDate,
        val endOfWeek: LocalDate
    )

    fun getCurrentMonthRange(zoneId: ZoneId = ZONE): MonthRange {
        val now = LocalDateTime.now(zoneId)
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        val startOfNextMonth = startOfMonth.plusMonths(1)
        return MonthRange(startOfMonth, startOfNextMonth)
    }

    fun monthRange(year: Int, month: Int, zoneId: ZoneId = ZONE): MonthRange {
        val ym = YearMonth.of(year, month)
        val startZ = ym.atDay(1).atStartOfDay(zoneId)
        val nextStartZ = startZ.plusMonths(1)
        return MonthRange(startZ.toLocalDateTime(), nextStartZ.toLocalDateTime())
    }

    fun thisWeekRange(today: LocalDate = LocalDate.now(ZONE)) : WeekRange {
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY))
        return WeekRange(startOfWeek, endOfWeek)
    }
}