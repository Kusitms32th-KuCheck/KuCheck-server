package onku.backend.domain.point.converter

import onku.backend.domain.point.MemberPointHistory
import java.time.format.DateTimeFormatter

object MemberPointConverter {
    private val dateFmt = DateTimeFormatter.ofPattern("MM/dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun toResponse(r: MemberPointHistory): onku.backend.domain.point.dto.MemberPointHistory =
        onku.backend.domain.point.dto.MemberPointHistory(
            date = r.occurredAt.toLocalDate().format(dateFmt),
            type = r.type,
            points = r.points,
            week = r.week,
            attendanceTime = r.attendanceTime?.format(timeFmt),
            earlyLeaveTime = r.earlyLeaveTime?.format(timeFmt)
        )
}
