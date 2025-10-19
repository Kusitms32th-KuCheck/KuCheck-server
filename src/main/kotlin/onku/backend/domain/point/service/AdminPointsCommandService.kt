package onku.backend.domain.point.service

import onku.backend.domain.member.repository.MemberRepository
import onku.backend.domain.point.repository.ManualPointRecordRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminPointsCommandService(
    private val manualPointRecordRepository: ManualPointRecordRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun updateStudyPoints(memberId: Long, studyPoints: Int) {
        val rec = manualPointRecordRepository.findByMemberId(memberId)
            ?: newManualRecord(memberId)
        rec.studyPoints = studyPoints
        manualPointRecordRepository.save(rec)
    }

    @Transactional
    fun updateKupportersPoints(memberId: Long, kuportersPoints: Int) {
        val rec = manualPointRecordRepository.findByMemberId(memberId)
            ?: newManualRecord(memberId)
        rec.kupportersPoints = kuportersPoints
        manualPointRecordRepository.save(rec)
    }

    @Transactional
    fun updateMemo(memberId: Long, memo: String) {
        val rec = manualPointRecordRepository.findByMemberId(memberId)
            ?: newManualRecord(memberId)
        rec.memo = memo
        manualPointRecordRepository.save(rec)
    }

    @Transactional
    fun updateIsTf(memberId: Long, isTf: Boolean) {
        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("Member not found: $memberId") }
        member.isTf = isTf
    }

    private fun newManualRecord(memberId: Long) =
        onku.backend.domain.point.ManualPoint(
            member = memberRepository.getReferenceById(memberId),
            studyPoints = 0,
            kupportersPoints = 0,
            memo = null
        )
}
