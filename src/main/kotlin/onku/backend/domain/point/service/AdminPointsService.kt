package onku.backend.domain.point.service

import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.dto.AdminPointsRowDto
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.global.page.PageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.max

@Service
class AdminPointsService(
    private val memberProfileRepository: MemberProfileRepository,
    private val attendanceRepository: AttendanceRepository,
    private val kupickRepository: KupickRepository,
    private val manualPointRecordRepository: ManualPointRepository
) {

    @Transactional(readOnly = true)
    fun getAdminOverview(
        year: Int,
        page: Int,
        size: Int
    ): PageResponse<AdminPointsRowDto> {
        val safePage = max(0, page)
        val pageRequest = PageRequest.of(safePage, size)

        val profilePage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageRequest)
        val memberIds = profilePage.content.mapNotNull(MemberProfile::memberId)
        if (memberIds.isEmpty()) return PageResponse.from(profilePage.map { emptyRow(year, it) })

        val startOfYear: LocalDateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0)
        val startOfNextYear: LocalDateTime = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0)

        // 출결 집계 (8~12월만)
        val attendances = attendanceRepository.findByMemberIdInAndAttendanceTimeBetween(
            memberIds, startOfYear, startOfNextYear
        )
        val monthlyAttendanceTotals: MutableMap<Long, MutableMap<Int, Int>> = mutableMapOf()
        attendances.forEach { a ->
            val m = a.attendanceTime.month.value
            if (m in 8..12) {
                val memberId = a.memberId
                val map = monthlyAttendanceTotals.getOrPut(memberId) { defaultMonthMapInt() }
                map[m] = (map[m] ?: 0) + a.status.points
            }
        }

        // 큐픽 참여 (8~12월만)
        val kupickRows = kupickRepository.findMemberMonthParticipation(memberIds, startOfYear, startOfNextYear)
        val kupickMonthMapByMember: MutableMap<Long, MutableMap<Int, Boolean>> = mutableMapOf()
        memberIds.forEach { id -> kupickMonthMapByMember[id] = defaultMonthMapBool() }
        kupickRows.forEach { row ->
            val memberId = (row[0] as Number).toLong()
            val m = (row[1] as Number).toInt()
            if (m in 8..12) {
                kupickMonthMapByMember[memberId]!![m] = true
            }
        }

        val manualPoints = manualPointRecordRepository.findByMemberIdIn(memberIds)
            .associateBy { it.memberId!! }

        val dtoPage = profilePage.map { profile ->
            val id = profile.memberId!!
            val member = profile.member
            val monthTotals = monthlyAttendanceTotals[id] ?: defaultMonthMapInt()
            val kupickMap = kupickMonthMapByMember[id] ?: defaultMonthMapBool()
            val manual = manualPoints[id]

            AdminPointsRowDto(
                memberId = id,
                name = profile.name,
                part = profile.part,
                phoneNumber = profile.phoneNumber,
                school = profile.school,
                major = profile.major,
                isTf = member.isTf,
                isStaff = member.isStaff,
                attendanceMonthlyTotals = monthTotals.toSortedMap(),
                kupickParticipation = kupickMap.toSortedMap(),
                studyPoints = manual?.studyPoints ?: 0,
                kuportersPoints = manual?.kupportersPoints ?: 0,
                memo = manual?.memo
            )
        }

        return PageResponse.from(dtoPage)
    }

    private fun defaultMonthMapInt(): MutableMap<Int, Int> =
        (8..12).associateWith { 0 }.toMutableMap()

    private fun defaultMonthMapBool(): MutableMap<Int, Boolean> =
        (8..12).associateWith { false }.toMutableMap()

    private fun emptyRow(year: Int, profile: onku.backend.domain.member.MemberProfile): AdminPointsRowDto {
        return AdminPointsRowDto(
            memberId = profile.memberId!!,
            name = profile.name,
            part = profile.part,
            phoneNumber = profile.phoneNumber,
            school = profile.school,
            major = profile.major,
            isTf = profile.member.isTf,
            isStaff = profile.member.isStaff,
            attendanceMonthlyTotals = (8..12).associateWith { 0 },
            kupickParticipation = (8..12).associateWith { false },
            studyPoints = 0,
            kuportersPoints = 0,
            memo = null
        )
    }
}