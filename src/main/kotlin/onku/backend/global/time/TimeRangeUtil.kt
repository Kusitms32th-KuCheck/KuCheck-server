package onku.backend.global.time

import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

object TimeRangeUtil {
    data class MonthRange(
        val startOfMonth: LocalDateTime,
        val startOfNextMonth: LocalDateTime
    )

    fun getCurrentMonthRange(zoneId: ZoneId = ZoneId.of("Asia/Seoul")): MonthRange {
        val now = LocalDateTime.now(zoneId)
        val startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
        val startOfNextMonth = startOfMonth.plusMonths(1)
        return MonthRange(startOfMonth, startOfNextMonth)
    }

    fun monthRange(year: Int, month: Int, zoneId: ZoneId = ZoneId.of("Asia/Seoul")): MonthRange {
        val ym = YearMonth.of(year, month)
        val startZ = ym.atDay(1).atStartOfDay(zoneId)
        val nextStartZ = startZ.plusMonths(1)
        return MonthRange(startZ.toLocalDateTime(), nextStartZ.toLocalDateTime())
    }
}