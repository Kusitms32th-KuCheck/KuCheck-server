package onku.backend.domain.point.converter

import onku.backend.domain.point.MemberPoint
import onku.backend.domain.point.dto.UserPointRecordResponse
import java.time.format.DateTimeFormatter

object MemberPointConverter {
    private val dateFmt = DateTimeFormatter.ofPattern("MM/dd")
    private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    fun toResponse(r: MemberPoint): UserPointRecordResponse =
        UserPointRecordResponse(
            date = r.occurredAt.toLocalDate().format(dateFmt),
            type = r.type,
            points = r.points,
            week = r.week,
            attendanceTime = r.attendanceTime?.format(timeFmt),
            earlyLeaveTime = r.earlyLeaveTime?.format(timeFmt)
        )
}
