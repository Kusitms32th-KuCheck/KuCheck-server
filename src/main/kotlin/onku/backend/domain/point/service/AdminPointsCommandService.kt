package onku.backend.domain.point.service

import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.member.MemberErrorCode
import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.ManualPoint
import onku.backend.domain.point.repository.ManualPointRepository
import onku.backend.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class AdminPointsCommandService(
    private val manualPointRecordRepository: ManualPointRepository,
    private val memberRepository: MemberRepository,
    private val kupickRepository: KupickRepository,
    private val clock: Clock
) {

    @Transactional
    fun updateStudyPoints(memberId: Long, studyPoints: Int) {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        rec.studyPoints = studyPoints
        manualPointRecordRepository.save(rec)
    }

    @Transactional
    fun updateKupportersPoints(memberId: Long, kuportersPoints: Int) {
        val rec = manualPointRecordRepository.findByMemberId(memberId) ?: newManualRecord(memberId)
        rec.kupportersPoints = kuportersPoints
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
        member.isTf = isTf
    }

    private fun newManualRecord(memberId: Long): ManualPoint {
        val memberRef = runCatching { memberRepository.getReferenceById(memberId) }
            .getOrElse {
                throw CustomException(MemberErrorCode.MEMBER_NOT_FOUND)
            }
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

        return kupickRepository.save(target).id
            ?: throw CustomException(KupickErrorCode.KUPICK_SAVE_FAILED)
    }
}
