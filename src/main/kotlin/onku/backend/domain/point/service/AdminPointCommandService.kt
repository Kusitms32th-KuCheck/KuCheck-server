package onku.backend.domain.point.service

import onku.backend.domain.attendance.AttendanceErrorCode
import onku.backend.domain.attendance.enums.AttendancePointType
import onku.backend.domain.attendance.repository.AttendanceRepository
import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.ManualPoint
import onku.backend.domain.point.MemberPointHistory
import onku.backend.domain.point.dto.*
import onku.backend.domain.point.enums.ManualPointType
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.domain.point.repository.MemberPointHistoryRepository
import onku.backend.domain.session.SessionErrorCode
import onku.backend.domain.session.repository.SessionRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@Service
class AdminPointCommandService(
    private val manualPointRecordRepository: ManualPointRepository,
    private val memberRepository: MemberRepository,
    private val kupickRepository: KupickRepository,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sessionRepository: SessionRepository,
    private val clock: Clock
) {

    @Transactional
    fun updateStudyPoints(memberId: Long, studyPoints: Int): StudyPointsResult {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        val before = rec.studyPoints ?: 0
        val after = studyPoints
        val diff = after - before
        if (diff != 0) {
            val now = LocalDateTime.now(clock)
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = rec.member,
                    manualType = ManualPointType.STUDY,
                    occurredAt = now,
                    points = diff
                )
            )
        }
        rec.studyPoints = after
        manualPointRecordRepository.save(rec)
        return StudyPointsResult(memberId = rec.member.id!!, studyPoints = after)
    }

    @Transactional
    fun updateKupportersPoints(memberId: Long, kupportersPoints: Int): KupportersPointsResult {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        val before = rec.kupportersPoints ?: 0
        val after = kupportersPoints
        val diff = after - before
        if (diff != 0) {
            val now = LocalDateTime.now(clock)
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = rec.member,
                    manualType = ManualPointType.KUPORTERS,
                    occurredAt = now,
                    points = diff
                )
            )
        }
        rec.kupportersPoints = after
        manualPointRecordRepository.save(rec)
        return KupportersPointsResult(memberId = rec.member.id!!, kupportersPoints = after)
    }

    @Transactional
    fun updateMemo(memberId: Long, memo: String): MemoResult {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        rec.memo = memo
        manualPointRecordRepository.save(rec)
        return MemoResult(memberId = rec.member.id!!, memo = rec.memo)
    }

    @Transactional
    fun updateIsTf(memberId: Long): Boolean {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val now = LocalDateTime.now(clock)
        val newValue = !member.isTf
        val delta = if (newValue) ManualPointType.TF.points else -ManualPointType.TF.points

        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.TF,
                occurredAt = now,
                points = delta
            )
        )
        member.isTf = newValue
        return newValue
    }

    @Transactional
    fun updateIsStaff(memberId: Long): Boolean {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val now = LocalDateTime.now(clock)
        val newValue = !member.isStaff
        val diff = if (newValue) ManualPointType.STAFF.points else -ManualPointType.STAFF.points

        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.STAFF,
                occurredAt = now,
                points = diff
            )
        )
        member.isStaff = newValue
        return newValue
    }

    @Transactional
    fun updateKupickApproval(memberId: Long, targetYm: YearMonth): KupickApprovalResult {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val now = LocalDateTime.now(clock)

        val startOfMonth = targetYm.atDay(1).atStartOfDay()
        val startOfNextMonth = targetYm.plusMonths(1).atDay(1).atStartOfDay()

        val existing = kupickRepository.findThisMonthByMember( // 기존 기록 조회
            member = member,
            start = startOfMonth,
            end = startOfNextMonth
        )

        val target = existing ?: run { // 없으면 생성
            val created = Kupick.createKupick(member, startOfMonth)
            kupickRepository.save(created)
        }

        val newApproved = !target.approval
        target.updateApproval(newApproved)

        val diff = if (newApproved) ManualPointType.KUPICK.points else -ManualPointType.KUPICK.points
        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.KUPICK,
                occurredAt = now,     // MemberPointHistory 레코드에는 수정시각(현재)로 기록
                points = diff
            )
        )

        val savedId = kupickRepository.save(target).id
            ?: throw CustomException(KupickErrorCode.KUPICK_SAVE_FAILED)

        return KupickApprovalResult(
            memberId = member.id!!,
            kupickId = savedId,
            isKupick = newApproved
        )
    }

    private fun newManualRecord(memberId: Long): ManualPoint {
        val memberRef = runCatching { memberRepository.getReferenceById(memberId) }
            .getOrElse { throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }
        return ManualPoint(
            member = memberRef,
            studyPoints = 0,
            kupportersPoints = 0,
            memo = null
        )
    }

    @Transactional
    fun updateAttendanceAndHistory(
        attendanceId: Long,
        memberId: Long,
        newStatus: AttendancePointType
    ): UpdateAttendanceStatusResponse {
        val attendance = attendanceRepository.findById(attendanceId)
            .orElseThrow { CustomException(AttendanceErrorCode.ATTENDANCE_NOT_FOUND) }

        if (attendance.memberId != memberId) {
            throw CustomException(AttendanceErrorCode.INVALID_MEMBER_FOR_ATTENDANCE)
        }

        val oldStatus = attendance.status
        if (oldStatus == newStatus) {
            val session = sessionRepository.findById(attendance.sessionId)
                .orElseThrow { CustomException(SessionErrorCode.SESSION_NOT_FOUND) }
            return UpdateAttendanceStatusResponse(
                attendanceId = attendanceId,
                memberId = memberId,
                oldStatus = oldStatus,
                newStatus = newStatus,
                diff = 0,
                week = session.week,
                occurredAt = LocalDateTime.now(clock)
            )
        }

        attendance.status = newStatus
        attendanceRepository.save(attendance)

        val diff = newStatus.points - oldStatus.points

        val session = sessionRepository.findById(attendance.sessionId)
            .orElseThrow { CustomException(SessionErrorCode.SESSION_NOT_FOUND) }

        if (diff != 0) {
            val now = LocalDateTime.now(clock)
            val member = memberRepository.findById(memberId)
                .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

            memberPointHistoryRepository.save(
                MemberPointHistory.ofAttendanceUpdate(
                    member = member,
                    status = newStatus,
                    occurredAt = now,
                    week = session.week,
                    diffPoint = diff,
                    time = null
                )
            )
        }
        return UpdateAttendanceStatusResponse(
            attendanceId = attendanceId,
            memberId = memberId,
            oldStatus = oldStatus,
            newStatus = newStatus,
            diff = diff,
            week = session.week,
            occurredAt = LocalDateTime.now(clock)
        )
    }
}