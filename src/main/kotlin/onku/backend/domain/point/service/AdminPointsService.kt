package onku.backend.domain.point.service

import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.repository.MemberProfileRepository
import onku.backend.domain.point.dto.*
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
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
        val safePageIndex = max(0, page)
        val pageRequest = PageRequest.of(safePageIndex, size)

        // 멤버 조회 (파트/이름 정렬 보장)
        val profilePage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageRequest)
        val memberIds = profilePage.content.mapNotNull(MemberProfile::memberId)
        if (memberIds.isEmpty()) return PageResponse.from(profilePage.map { emptyOverviewRow(it) })

        // 조회 구간 설정: 8월 ~ 12월
        val startOfAug: LocalDateTime = LocalDateTime.of(year, Month.AUGUST, 1, 0, 0, 0)
        val endExclusive: LocalDateTime = LocalDateTime.of(year + 1, 1, 1, 0, 0, 0)

        // 출석 레코드 → 월별 포인트 합산
        val monthlyAttendanceTotals: MutableMap<Long, MutableMap<Int, Int>> = mutableMapOf()
        attendanceRepository.findByMemberIdInAndAttendanceTimeBetween(memberIds, startOfAug, endExclusive)
            .forEach { attendance ->
                val month = attendance.attendanceTime.month.value
                val mapForMember = monthlyAttendanceTotals.getOrPut(attendance.memberId) { initMonthScoreMap() }
                mapForMember[month] = (mapForMember[month] ?: 0) + attendance.status.points // 동일 월 포인트 합산
            }

        // 3) 큐픽 참여 여부를 월별로 표시 (기본 false → 참여 시 true)
        val kupickParticipationByMember: MutableMap<Long, MutableMap<Int, Boolean>> = mutableMapOf()
        memberIds.forEach { id -> kupickParticipationByMember[id] = initMonthParticipationMap() }
        kupickRepository.findMemberMonthParticipation(memberIds, startOfAug, endExclusive)
            .forEach { row ->
                val memberId = (row[0] as Number).toLong()
                val month = (row[1] as Number).toInt()
                if (month in 8..12) {
                    kupickParticipationByMember[memberId]!![month] = true
                }
            }

        // 스터디/큐포터즈/메모 조회
        val manualPointsByMember = manualPointRecordRepository.findByMemberIdIn(memberIds)
            .associateBy { it.memberId!! }

        // 페이지 단위 DTO 변환
        val dtoPage = profilePage.map { profile ->
            val memberId = profile.memberId!!
            val monthTotals = monthlyAttendanceTotals[memberId] ?: initMonthScoreMap()
            val kupickMap = kupickParticipationByMember[memberId] ?: initMonthParticipationMap()
            val manual = manualPointsByMember[memberId]

            AdminPointsRowDto(
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

    private fun emptyOverviewRow(profile: onku.backend.domain.member.MemberProfile): AdminPointsRowDto {
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

        // 조회 구간 설정
        val zone: ZoneId = clock.zone
        val startZdt = ZonedDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN, zone)
        val endZdt = startZdt.plusMonths(1)
        val start = startZdt.toLocalDateTime()
        val end = endZdt.toLocalDateTime()

        // 세션 시작일 (중복 제거/오름차순)
        val sessionDates: List<LocalDate> = sessionRepository.findStartTimesBetween(start, end)
            .map { it.toLocalDate() }
            .distinct()
            .sorted()
        val sessionDays: List<Int> = sessionDates.map { it.dayOfMonth }

        // 멤버 조회
        val pageable = PageRequest.of(page, size)
        val memberPage = memberProfileRepository.findAllByOrderByPartAscNameAsc(pageable)
        val pageMemberIds = memberPage.content.mapNotNull { it.memberId }
        if (pageMemberIds.isEmpty()) {
            throw CustomException(MemberErrorCode.PAGE_MEMBERS_NOT_FOUND)
        }

        // 해당 페이지 멤버의 월간 출석 레코드 조회
        data class Row(
            val memberId: Long,
            val date: LocalDate,
            val attendanceId: Long?,
            val status: onku.backend.domain.attendance.enums.AttendanceStatus?,
            val point: Int?
        )

        val rows: List<Row> = attendanceRepository
            .findByMemberIdInAndAttendanceTimeBetween(pageMemberIds, start, end)
            .map { a ->
                Row(
                    memberId = a.memberId,
                    date = a.attendanceTime.toLocalDate(),
                    attendanceId = a.id,
                    status = a.status,
                    point = a.status.points
                )
            }

        val rowsByMember = rows.groupBy { it.memberId } // 멤버별로 레코드 그룹핑

        // 멤버별 일자 정렬 + 세션일 기준 결측 레코드 채움
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

            // 세션일이 존재하지만 기록이 없는 날짜는 null 처리
            if (sessionDates.isNotEmpty()) {
                val recordedDates = baseRecords.map { it.date }.toSet()
                sessionDates.filter { it !in recordedDates }.forEach { date ->
                    baseRecords.add(
                        AttendanceRecordDto(
                            date = date,
                            attendanceId = null,
                            status = null,
                            point = null
                        )
                    )
                }
                baseRecords.sortBy { it.date } // 날짜 오름차순 정렬
            }

            MemberMonthlyAttendanceDto(
                memberId = memberId,
                name = profile.name ?: "Unknown",
                records = baseRecords
            )
        }

        // 멤버 페이지 순서를 유지하며 DTO 페이지 구성
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