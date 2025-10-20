package onku.backend.domain.point.service

import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.dto.*
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.page.PageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import kotlin.math.max

@Service
class AdminPointsService(
    private val memberProfileRepository: MemberProfileRepository,
    private val attendanceRepository: AttendanceRepository,
    private val kupickRepository: KupickRepository,
    private val manualPointRecordRepository: ManualPointRepository,
    private val sessionRepository: SessionRepository,
    private val clock: Clock
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

    @Transactional(readOnly = true)
    fun getMonthlyPaged(
        year: Int,
        month: Int,
        page: Int,
        size: Int
    ): MonthlyAttendancePageResponse {
        require(month in 8..12) { "month must be 8..12" }

        val zone: ZoneId = clock.zone
        val startZdt = ZonedDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN, zone)
        val endZdt = startZdt.plusMonths(1)
        val start = startZdt.toLocalDateTime()
        val end = endZdt.toLocalDateTime()

        val sessionDates: List<LocalDate> = sessionRepository.findStartTimesBetween(start, end)
            .map { it.toLocalDate() }
            .distinct()
            .sorted()
        val sessionDays: List<Int> = sessionDates.map { it.dayOfMonth }

        val pageable = PageRequest.of(page, size)
        val memberPage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageable)

        val pageMemberIds = memberPage.content.mapNotNull { it.memberId }
        if (pageMemberIds.isEmpty()) {
            return MonthlyAttendancePageResponse(
                year = year,
                month = month,
                sessionDates = sessionDays,
                members = PageResponse.from(
                    memberPage.map { p ->
                        MemberMonthlyAttendanceDto(
                            memberId = p.memberId!!,
                            name = p.name ?: "Unknown",
                            records = emptyList()
                        )
                    }
                )
            )
        }

        val attendances = attendanceRepository
            .findByMemberIdInAndAttendanceTimeBetween(pageMemberIds, start, end)

        data class Row(
            val memberId: Long,
            val date: LocalDate,
            val attendanceId: Long?,
            val status: onku.backend.domain.attendance.enums.AttendanceStatus?,
            val point: Int?
        )

        val rows: List<Row> = attendances.map { a ->
            Row(
                memberId = a.memberId,
                date = a.attendanceTime.toLocalDate(),
                attendanceId = a.id,
                status = a.status,
                point = a.status.points
            )
        }

        val nameById = memberPage.content.associate { it.memberId!! to (it.name ?: "Unknown") }
        val byMember = rows.groupBy { it.memberId }

        val memberDtos = memberPage.content.map { profile ->
            val mid = profile.memberId!!
            val base = byMember[mid]
                ?.sortedBy { it.date }
                ?.map {
                    AttendanceRecordDto(
                        date = it.date,
                        attendanceId = it.attendanceId,
                        status = it.status,
                        point = it.point
                    )
                }
                ?.toMutableList()
                ?: mutableListOf()

            if (sessionDates.isNotEmpty()) {
                val recorded = base.map { it.date }.toSet()
                sessionDates.filter { it !in recorded }.forEach { d ->
                    base.add(
                        AttendanceRecordDto(
                            date = d,
                            attendanceId = null,
                            status = null,
                            point = null
                        )
                    )
                }
                base.sortBy { it.date }
            }

            MemberMonthlyAttendanceDto(
                memberId = mid,
                name = nameById[mid] ?: "Unknown",
                records = base
            )
        }

        val dtoPage = memberPage.map { p ->
            memberDtos.first { it.memberId == p.memberId }
        }

        return MonthlyAttendancePageResponse(
            year = year,
            month = month,
            sessionDates = sessionDays,
            members = PageResponse.from(dtoPage)
        )
    }
}