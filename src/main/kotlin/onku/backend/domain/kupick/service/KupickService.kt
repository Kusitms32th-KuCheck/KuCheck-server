package onku.backend.domain.kupick.service

import onku.backend.domain.kupick.Kupick
import onku.backend.domain.kupick.KupickErrorCode
import onku.backend.domain.kupick.dto.KupickFcmInfo
import onku.backend.domain.kupick.repository.KupickRepository
import onku.backend.domain.kupick.repository.projection.KupickUrls
import onku.backend.domain.kupick.repository.projection.KupickWithProfile
import onku.backend.domain.member.Member
import onku.backend.global.exception.CustomException
import onku.backend.global.time.TimeRangeUtil
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class KupickService(
    private val kupickRepository: KupickRepository,
) {
    @Transactional
    fun submitApplication(member: Member, applicationUrl : String) : String? {
        val monthObject = TimeRangeUtil.getCurrentMonthRange()
        val existing = kupickRepository.findFirstByMemberAndApplicationDateBetween(
            member, monthObject.startOfMonth, monthObject.startOfNextMonth
        )

        val now = LocalDateTime.now()

        return if (existing != null) {
            if(existing.approval) {
                throw CustomException(KupickErrorCode.KUPICK_NOT_UPDATE)
            }
            val old = existing.applicationImageUrl
            existing.updateApplication(applicationUrl, now)
            old
        } else {
            kupickRepository.save(Kupick.createApplication(member, applicationUrl, now))
            null
        }
    }

    @Transactional
    fun submitView(member: Member, viewUrl: String) : String? {
        val monthObject = TimeRangeUtil.getCurrentMonthRange()
        val now = LocalDateTime.now()
        val kupick = kupickRepository.findThisMonthByMember(
            member,
            monthObject.startOfMonth,
            monthObject.startOfNextMonth) ?: throw CustomException(KupickErrorCode.KUPICK_APPLICATION_FIRST)
        val oldViewUrl = kupick.viewImageUrl
        if(kupick.approval) {
            throw CustomException(KupickErrorCode.KUPICK_NOT_UPDATE)
        }
        kupick.submitView(viewUrl, now)
        return oldViewUrl
    }

    @Transactional(readOnly = true)
    fun viewMyKupick(member: Member) : KupickUrls? {
        val monthObject = TimeRangeUtil.getCurrentMonthRange()
        return kupickRepository.findUrlsForMemberInMonth(
            member,
            monthObject.startOfMonth,
            monthObject.startOfNextMonth
        )
    }

    @Transactional(readOnly = true)
    fun findAllAsShowUpdateResponse(year : Int, month : Int): List<KupickWithProfile> {
        val monthObject = TimeRangeUtil.monthRange(year, month)
        return kupickRepository.findAllWithProfile(
            monthObject.startOfMonth,
            monthObject.startOfNextMonth,
        )
    }

    @Transactional
    fun decideApproval(kupickId: Long, approval: Boolean) {
        val kupick = kupickRepository.findByIdOrNull(kupickId)
            ?: throw CustomException(KupickErrorCode.KUPICK_NOT_FOUND)
        kupick.updateApproval(approval)
    }

    @Transactional(readOnly = true)
    fun findFcmInfo(kupickId: Long) : KupickFcmInfo? {
        return kupickRepository.findFcmInfoByKupickId(kupickId)
    }
}