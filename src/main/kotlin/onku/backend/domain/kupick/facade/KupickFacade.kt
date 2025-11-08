package onku.backend.domain.kupick.facade

import onku.backend.domain.kupick.dto.request.KupickApprovalRequest
import onku.backend.domain.kupick.dto.response.ShowUpdateResponseDto
import onku.backend.domain.kupick.dto.response.ViewMyKupickResponseDto
import onku.backend.domain.kupick.service.KupickService
import onku.backend.domain.member.Member
import onku.backend.global.alarm.AlarmMessage
import onku.backend.global.alarm.AlarmTitle
import onku.backend.global.alarm.FCMService
import onku.backend.global.s3.dto.GetUpdateAndDeleteUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.stereotype.Component

@Component
class KupickFacade(
    private val s3Service: S3Service,
    private val kupickService: KupickService,
    private val fcmService: FCMService
) {
    fun submitApplication(member: Member, fileName: String): GetUpdateAndDeleteUrlDto {
        val signedUrlDto = s3Service.getPostS3Url(member.id!!, fileName, FolderName.KUPICK_APPLICATION.name, UploadOption.IMAGE)
        val oldDeletePreSignedUrl = kupickService
            .submitApplication(member, signedUrlDto.key)
            ?.let { oldKey -> s3Service.getDeleteS3Url(oldKey).preSignedUrl }
            ?: ""
        return GetUpdateAndDeleteUrlDto(
            signedUrlDto.preSignedUrl,
            oldDeletePreSignedUrl
        )
    }

    fun submitView(member: Member, fileName: String): GetUpdateAndDeleteUrlDto {
        val signedUrlDto = s3Service.getPostS3Url(member.id!!, fileName, FolderName.KUPICK_VIEW.name, UploadOption.IMAGE)
        val oldDeletePreSignedUrl = kupickService
            .submitView(member, signedUrlDto.key)
            ?.let { oldKey -> s3Service.getDeleteS3Url(oldKey).preSignedUrl }
            ?: ""
        return GetUpdateAndDeleteUrlDto(
            signedUrlDto.preSignedUrl,
            oldDeletePreSignedUrl
        )
    }

    fun viewMyKupick(member: Member): ViewMyKupickResponseDto {
        val urls = kupickService.viewMyKupick(member)
        val s3GetApplicationImageUrl = urls?.getApplicationImageUrl()
            ?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }
        val s3GetViewImageUrl = urls?.getViewImageUrl()
            ?.let { s3Service.getGetS3Url(member.id!!, it).preSignedUrl }
        return ViewMyKupickResponseDto.of(
            s3GetApplicationImageUrl,
            urls?.getApplicationDate(),
            s3GetViewImageUrl,
            urls?.getViewDate()
        )
    }

    fun showUpdate(year : Int, month : Int): List<ShowUpdateResponseDto> {
        val profiles = kupickService.findAllAsShowUpdateResponse(year, month)
        val dtoList = profiles.map { p ->
            val memberId = p.memberProfile.memberId!!
            val profile = p.memberProfile

            val applicationUrl: String? =
                p.kupick.applicationImageUrl
                    ?.takeIf { it.isNotBlank() } //여기서 만약 applicationImageUrl이 null값이 들어가면 에러가 남(전체 학회원 큐픽 조회에 영향을 끼침) 그래서 safe call 추가
                    ?.let { key -> s3Service.getGetS3Url(memberId, key).preSignedUrl }
            val viewUrl: String? =
                p.kupick.viewImageUrl
                    ?.takeIf { it.isNotBlank() }
                    ?.let { key -> s3Service.getGetS3Url(memberId, key).preSignedUrl }

            ShowUpdateResponseDto(
                name = profile.name,
                part = profile.part,
                kupickId = p.kupick.id,
                submitDate = p.kupick.submitDate,
                applicationUrl = applicationUrl,
                viewUrl = viewUrl,
                approval = p.kupick.approval
            )
        }
        return dtoList
    }

    fun decideApproval(kupickApprovalRequest: KupickApprovalRequest): Boolean {
        kupickService.decideApproval(kupickApprovalRequest.kupickId, kupickApprovalRequest.approval)
        val info = kupickService.findFcmInfo(kupickApprovalRequest.kupickId)
        val fcmToken = info?.fcmToken
        val submitMonth = info?.submitDate?.monthValue
        fcmToken?.let {
            fcmService.sendMessageTo(
                targetToken = it,
                title = AlarmTitle.KUPICK,
                body = AlarmMessage.kupick(submitMonth!!, kupickApprovalRequest.approval),
                link = null
            )
        }
        return true
    }
}