package onku.backend.domain.point.service

import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.ManualPoint
import onku.backend.domain.point.MemberPointHistory
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
    fun updateStudyPoints(memberId: Long, studyPoints: Int) {
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
    }

    @Transactional
    fun updateKupportersPoints(memberId: Long, kuportersPoints: Int) {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        val before = rec.kupportersPoints ?: 0
        val after = kuportersPoints
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
    }

    @Transactional
    fun updateMemo(memberId: Long, memo: String) {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        rec.memo = memo
        manualPointRecordRepository.save(rec)
    }

    @Transactional
    fun updateIsTf(memberId: Long, isTf: Boolean) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (member.isTf != isTf) {
            val now = LocalDateTime.now(clock)
            val delta = if (isTf) ManualPointType.TF.points else -ManualPointType.TF.points
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = member,
                    manualType = ManualPointType.TF,
                    occurredAt = now,
                    points = delta
                )
            )
            member.isTf = isTf
        }
    }

    @Transactional
    fun updateIsStaff(memberId: Long, isStaff: Boolean) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { CustomException(MemberErrorCode.MEMBER_NOT_FOUND) }

        if (member.isStaff != isStaff) {
            val now = LocalDateTime.now(clock)
            val delta = if (isStaff) ManualPointType.STAFF.points else -ManualPointType.STAFF.points
            memberPointHistoryRepository.save(
                MemberPointHistory.ofManual(
                    member = member,
                    manualType = ManualPointType.STAFF,
                    occurredAt = now,
                    points = delta
                )
            )
            member.isStaff = isStaff
        }
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
    fun updateKupickApproval(memberId: Long, isKupick: Boolean): Long {
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

        target.updateApproval(isKupick)

        val points = if (isKupick) ManualPointType.KUPICK.points else -ManualPointType.KUPICK.points
        memberPointHistoryRepository.save(
            MemberPointHistory.ofManual(
                member = member,
                manualType = ManualPointType.KUPICK,
                occurredAt = now,
                points = points
            )
        )

        return kupickRepository.save(target).id
            ?: throw CustomException(KupickErrorCode.KUPICK_SAVE_FAILED)
    }
}