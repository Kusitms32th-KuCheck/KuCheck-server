package onku.backend.domain.point.service

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
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AdminPointCommandService(
    private val manualPointRecordRepository: ManualPointRepository,
    private val memberRepository: MemberRepository,
    private val kupickRepository: KupickRepository,
    private val memberPointHistoryRepository: MemberPointHistoryRepository,
    private val clock: Clock
) {

    @Transactional
    fun updateStudyPoints(memberId: Long, studyPoints: Int): StudyPointsResult {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        val before = rec.studyPoints ?: 0
        val after = studyPoints
        val delta = after - before
        if (delta != 0) {
            val now = LocalDateTime.now(clock)
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = rec.member,
                    manualType = ManualPointType.STUDY,
                    occurredAt = now,
                    points = delta
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
        val delta = after - before
        if (delta != 0) {
            val now = LocalDateTime.now(clock)
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = rec.member,
                    manualType = ManualPointType.KUPORTERS,
                    occurredAt = now,
                    points = delta
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
        val delta = if (newValue) ManualPointType.STAFF.points else -ManualPointType.STAFF.points

        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.STAFF,
                occurredAt = now,
                points = delta
            )
        )
        member.isStaff = newValue
        return newValue
    }

    @Transactional
    fun updateKupickApproval(memberId: Long): KupickApprovalResult {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        val now = LocalDateTime.now(clock)
        val startOfMonth = LocalDate.now(clock).withDayOfMonth(1).atStartOfDay()
        val startOfNextMonth = startOfMonth.toLocalDate().plusMonths(1).atStartOfDay()

        val existing = kupickRepository.findThisMonthByMember(
            member = member,
            start = startOfMonth,
            end = startOfNextMonth
        )

        val target = existing ?: run {
            val created = Kupick.createKupick(member, now)
            kupickRepository.save(created)
        }

        val newApproved = !target.approval
        target.updateApproval(newApproved)

        val delta = if (newApproved) ManualPointType.KUPICK.points else -ManualPointType.KUPICK.points
        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.KUPICK,
                occurredAt = now,
                points = delta
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
}