package onku.backend.domain.point.service

import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.dto.AdminPointOverviewDto
import onku.backend.domain.point.dto.AttendanceRecordDto
import onku.backend.domain.point.dto.MemberMonthlyAttendanceDto
import onku.backend.domain.point.dto.MonthlyAttendancePageResponse
import onku.backend.domain.point.enums.PointCategory
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import onku.backend.global.page.PageResponse
import onku.backend.global.time.TimeRangeUtil
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.max

@Service
class AdminPointService(
    private val memberProfileRepository: MemberProfileRepository,
    private val kupickRepository: KupickRepository,
    private val manualPointRecordRepository: ManualPointRepository,
    private val sessionRepository: SessionRepository,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val clock: Clock
) {

    @Transactional(readOnly = true)
    fun getAdminOverview(
        year: Int,
        page: Int,
        size: Int
    ): PageResponse<AdminPointOverviewDto> {
        val safePageIndex = max(0, page)
        val pageRequest = PageRequest.of(safePageIndex, size)

        // 멤버 조회 (파트/이름 정렬 보장)
        val profilePage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageRequest)
        val memberIds = profilePage.content.mapNotNull(MemberProfile::memberId)
        if (memberIds.isEmpty()) return PageResponse.from(profilePage.map { emptyOverviewRow(it) })

        // 조회 구간 설정: 8월 ~ 12월
        val augRange = TimeRangeUtil.monthRange(year, 8, clock.zone)
        val decRange = TimeRangeUtil.monthRange(year, 12, clock.zone)
        val startOfAug: LocalDateTime = augRange.startOfMonth
        val endExclusive: LocalDateTime = decRange.startOfNextMonth

        // 출석 레코드 → 월별 포인트 합산 (MemberPointHistory 기반)
        val monthlyAttendanceTotals: MutableMap<Long, MutableMap<Int, Int>> =
            memberIds.associateWith { initMonthScoreMap() }.toMutableMap()

        memberPointHistoryRepository
            .sumAttendanceByMemberAndMonth(
                memberIds = memberIds,
                category = PointCategory.ATTENDANCE,
                start = startOfAug,
                end = endExclusive
            )
            .forEach { row ->
                val mId = row.getMemberId()
                val month = row.getMonth()
                if (month in 8..12) {
                    val mapForMember = monthlyAttendanceTotals.getOrPut(mId) { initMonthScoreMap() }
                    mapForMember[month] = (mapForMember[month] ?: 0) + row.getPoints().toInt()
                }
            }

        // 큐픽 참여 여부
        val kupickParticipationByMember =
            memberIds.associateWith { initMonthParticipationMap() }.toMutableMap()

        kupickRepository.findMemberMonthParticipation(memberIds, startOfAug, endExclusive)
            .forEach { row ->
                val memberId = (row[0] as Number).toLong()
                val month = (row[1] as Number).toInt()
                if (month in 8..12) {
                    kupickParticipationByMember[memberId]!![month] = true
                }
            }
        val manualPointsByMember = manualPointRecordRepository.findByMemberIdIn(memberIds)
            .associateBy { it.memberId!! }

        val dtoPage = profilePage.map { profile ->
            val memberId = profile.memberId!!
            val monthTotals = monthlyAttendanceTotals[memberId] ?: initMonthScoreMap()
            val kupickMap = kupickParticipationByMember[memberId] ?: initMonthParticipationMap()
            val manual = manualPointsByMember[memberId]

            AdminPointOverviewDto(
                memberId = memberId,
                name = profile.name,
                part = profile.part,
                phoneNumber = profile.phoneNumber,
                school = profile.school,
                major = profile.major,
                isTf = profile.member.isTf,
                isStaff = profile.member.isStaff,
                attendanceMonthlyTotals = monthTotals.toSortedMap(),
                kupickParticipation = kupickMap.toSortedMap(),
                studyPoints = manual?.studyPoints ?: 0,
                kuportersPoints = manual?.kupportersPoints ?: 0,
                memo = manual?.memo
            )
        }

        return PageResponse.from(dtoPage)
    }

    private fun initMonthScoreMap(): MutableMap<Int, Int> =
        (8..12).associateWith { 0 }.toMutableMap()

    private fun initMonthParticipationMap(): MutableMap<Int, Boolean> =
        (8..12).associateWith { false }.toMutableMap()

    private fun emptyOverviewRow(profile: onku.backend.domain.member.MemberProfile): AdminPointOverviewDto {
        return AdminPointOverviewDto(
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

        // 조회 구간
        val monthRange = TimeRangeUtil.monthRange(year, month, clock.zone)
        val start: LocalDateTime = monthRange.startOfMonth
        val end: LocalDateTime = monthRange.startOfNextMonth

        // 세션 시작일
        val startDate: LocalDate = start.toLocalDate()
        val endDateInclusive: LocalDate = end.minusNanos(1).toLocalDate()

        val startParts = sessionRepository.findStartDateAndTimeBetweenDates(startDate, endDateInclusive)

        val sessionStartDateTimes: List<LocalDateTime> = startParts
            .map { LocalDateTime.of(it.getStartDate(), it.getStartTime()) }
            .filter { dt -> !dt.isBefore(start) && dt.isBefore(end) }

        val sessionDates: List<LocalDate> = sessionStartDateTimes
            .map { it.toLocalDate() }
            .distinct()
            .sorted()
        val sessionDays: List<Int> = sessionDates.map { it.dayOfMonth }

        // 멤버 조회
        val pageable = PageRequest.of(page, size)
        val memberPage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageable)
        val pageMemberIds = memberPage.content.mapNotNull { it.memberId }
        if (pageMemberIds.isEmpty()) throw CustomException(MemberErrorCode.PAGE_MEMBERS_NOT_FOUND)

        val historyRecords = memberPointHistoryRepository.findAttendanceByMemberIdsBetween(
            memberIds = pageMemberIds,
            category = PointCategory.ATTENDANCE,
            start = start,
            end = end
        )

        val attendanceList = attendanceRepository.findByMemberIdInAndAttendanceTimeBetween(
            pageMemberIds, start, end
        )
        val attendanceIdByMemberDate: Map<Pair<Long, LocalDate>, Long> =
            attendanceList.associateBy(
                { it.memberId to it.attendanceTime.toLocalDate() },
                { it.id!! }
            )

        data class Row(
            val memberId: Long,
            val date: LocalDate,
            val attendanceId: Long?,
            val status: AttendancePointType,
            val point: Int
        )

        val rows: List<Row> = historyRecords.map { r ->
            val memberId = r.member.id!!
            val date = r.occurredAt.toLocalDate()
            val attId = attendanceIdByMemberDate[memberId to date]
            Row(
                memberId = memberId,
                date = date,
                attendanceId = attId,
                status = AttendancePointType.valueOf(r.type),
                point = r.points
            )
        }

        val rowsByMember = rows.groupBy { it.memberId }

        val memberDtos = memberPage.content.map { profile ->
            val memberId = profile.memberId!!
            val baseRecords = rowsByMember[memberId]
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
                val recordedDates = baseRecords.map { it.date }.toSet()
                sessionDates.filter { it !in recordedDates }.forEach { date ->
                    baseRecords.add(
                        AttendanceRecordDto(
                            date = date,
                            attendanceId = attendanceIdByMemberDate[memberId to date],
                            status = null,
                            point = null
                        )
                    )
                }
                baseRecords.sortBy { it.date }
            }

            MemberMonthlyAttendanceDto(
                memberId = memberId,
                name = profile.name ?: "Unknown",
                records = baseRecords
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