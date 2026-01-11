package onku.backend.domain.notice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import onku.backend.domain.member.Member
import onku.backend.domain.notice.NoticeAttachment
import onku.backend.domain.notice.dto.notice.PresignedUploadResponse
import onku.backend.domain.notice.repository.NoticeAttachmentRepository
import onku.backend.global.s3.dto.GetS3UrlDto
import onku.backend.global.s3.enums.FolderName
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service

@Service
@Transactional(readOnly = true)
class NoticeAttachmentService(
    private val noticeAttachmentRepository: NoticeAttachmentRepository,
    private val s3Service: S3Service
) {

    @Transactional
    fun prepareUpload(
        currentMember: Member,
        filename: String,
        fileType: UploadOption,
        fileSize: Long,
    ): PresignedUploadResponse {

        val put: GetS3UrlDto = s3Service.getPostS3Url(
            memberId = currentMember.id!!,
            filename = filename,
            folderName = FolderName.NOTICE.name,
            option = fileType
        )

        val file = noticeAttachmentRepository.save(
            NoticeAttachment(
                notice = null,
                s3Key = put.key,
                attachmentType = fileType,
                attachmentSize = fileSize
            )
        )

        return PresignedUploadResponse(
            fileId = file.id!!,
            presignedUrl = put.preSignedUrl
        )
    }
}