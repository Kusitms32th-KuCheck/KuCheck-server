package onku.backend.domain.kupick.facade

import onku.backend.domain.kupick.dto.ShowUpdateResponseDto
import onku.backend.domain.kupick.dto.ViewMyKupickResponseDto
import onku.backend.domain.kupick.service.KupickService
import onku.backend.domain.member.Member
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.service.S3Service
import org.springframework.stereotype.Component

@Component
class KupickFacade(
    private val s3Service: S3Service,
    private val kupickService: KupickService,
) {
    fun submitApplication(member: Member, fileName: String): GetPreSignedUrlDto {
        val signedUrlDto = s3Service.getPostS3Url(member.id!!, fileName, FolderName.KUPICK_APPLICATION.name)
        kupickService.submitApplication(member, signedUrlDto.key)
        return GetPreSignedUrlDto(
            signedUrlDto.preSignedUrl
        )
    }

    fun submitView(member: Member, fileName: String): GetPreSignedUrlDto {
        val signedUrlDto = s3Service.getPostS3Url(member.id!!, fileName, FolderName.KUPICK_VIEW.name)
        kupickService.submitView(member, signedUrlDto.key)
        return GetPreSignedUrlDto(
            signedUrlDto.preSignedUrl
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
        return profiles.map { p ->
            val memberId = p.memberProfile.memberId!!              // presign에 사용
            val profile = p.memberProfile         // MemberProfile

            val applicationUrl: String? =
                p.kupick.applicationImageUrl
                    .takeIf { it.isNotBlank() }
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
    }
}