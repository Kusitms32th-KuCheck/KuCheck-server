package onku.backend.domain.notice.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import onku.backend.domain.member.Member
import onku.backend.domain.notice.Notice
import onku.backend.domain.notice.NoticeErrorCode
import onku.backend.domain.notice.NoticeAttachment
import onku.backend.domain.notice.dto.notice.*
import onku.backend.domain.notice.repository.NoticeCategoryRepository
import onku.backend.domain.notice.repository.NoticeAttachmentRepository
import onku.backend.domain.notice.repository.NoticeRepository
import onku.backend.domain.notice.util.NoticeDtoMapper
import onku.backend.global.exception.CustomException
import onku.backend.global.page.PageResponse
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import java.time.LocalDateTime
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

@Service
@Transactional(readOnly = true)
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val categoryRepository: NoticeCategoryRepository,
    private val noticeAttachmentRepository: NoticeAttachmentRepository,
    private val s3Service: S3Service
) {

    fun list(
        currentMember: Member,
        page: Int,
        size: Int,
        categoryId: Long?
    ): PageResponse<NoticeListItemResponse> {

        val memberId = currentMember.id!!

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("publishedAt"),
                Sort.Order.desc("id")
            )
        )

        val noticePage = if (categoryId == null) {
            noticeRepository.findAllByOrderByPublishedAtDescIdDesc(pageable)
        } else {
            // 해당 카테고리를 포함하는 공지 검색
            noticeRepository.findDistinctByCategoriesIdOrderByPublishedAtDescIdDesc(
                categoryId,
                pageable
            )
        }

        val items = noticePage.content.map { n ->
            val (imageFiles, fileFiles) = splitPresignedUrls(memberId, n.attachments)
            NoticeDtoMapper.toListItem(n, imageFiles, fileFiles)
        }

        return PageResponse(
            data = items,
            totalPages = noticePage.totalPages,
            isLastPage = noticePage.isLast
        )
    }

    fun search(
        keyword: String,
        page: Int,
        size: Int
    ): PageResponse<NoticeListItemResponse> {
        val memberId = 0L
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(
                Sort.Order.desc("publishedAt"),
                Sort.Order.desc("id")
            )
        )
        val trimmedKeyword = keyword.trim()
        val noticePage = if (trimmedKeyword.isBlank()) {
            noticeRepository.findAllByOrderByPublishedAtDescIdDesc(pageable)
        } else { // 제목 OR 내용에 keyword 포함되는 공지 검색
            noticeRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByPublishedAtDescIdDesc(
                    trimmedKeyword,
                    trimmedKeyword,
                    pageable
                )
        }
        val items = noticePage.content.map { n ->
            val (imageFiles, fileFiles) = splitPresignedUrls(memberId, n.attachments)
            NoticeDtoMapper.toListItem(n, imageFiles, fileFiles)
        }
        return PageResponse(
            data = items,
            totalPages = noticePage.totalPages,
            isLastPage = noticePage.isLast
        )
    }

    fun get(noticeId: Long, currentMember: Member): NoticeDetailResponse {
        val n = noticeRepository.findById(noticeId)
            .orElseThrow { CustomException(NoticeErrorCode.NOTICE_NOT_FOUND) }

        val memberId = currentMember.id!!
        val (imageFiles, fileFiles) = splitPresignedUrls(memberId, n.attachments)

        return NoticeDtoMapper.toDetail(n, imageFiles, fileFiles)
    }

    @Transactional
    fun create(currentMember: Member, req: NoticeCreateRequest): NoticeDetailResponse {
        val categories = categoryRepository.findAllById(req.categoryIds)
        if (categories.size != req.categoryIds.size) {
            throw CustomException(NoticeErrorCode.CATEGORY_NOT_FOUND)
        }

        val notice = noticeRepository.save(
            Notice(
                member = currentMember,
                title = req.title,
                content = req.content,
                publishedAt = LocalDateTime.now()
            )
        )
        notice.categories.addAll(categories.toSet())

        if (req.fileIds.isNotEmpty()) {
            val files = noticeAttachmentRepository.findAllById(req.fileIds)
            files.forEach { notice.addFile(it) }
        }

        val memberId = currentMember.id!!
        val (imageFiles, fileFiles) = splitPresignedUrls(memberId, notice.attachments)

        return NoticeDtoMapper.toDetail(notice, imageFiles, fileFiles)
    }

    @Transactional
    fun update(noticeId: Long, currentMember: Member, req: NoticeUpdateRequest): NoticeDetailResponse {
        val notice = noticeRepository.findById(noticeId)
            .orElseThrow { CustomException(NoticeErrorCode.NOTICE_NOT_FOUND) }

        val categories = categoryRepository.findAllById(req.categoryIds)
        if (categories.size != req.categoryIds.size) {
            throw CustomException(NoticeErrorCode.CATEGORY_NOT_FOUND)
        }

        notice.title = req.title
        notice.content = req.content
        notice.categories.clear()
        notice.categories.addAll(categories.toSet())

        val newFiles: List<NoticeAttachment> =
            if (req.fileIds.isNotEmpty()) {
                noticeAttachmentRepository.findAllById(req.fileIds)
            } else {
                emptyList()
            }
        notice.clearFiles()
        newFiles.forEach { notice.addFile(it) }

        val memberId = currentMember.id!!
        val (imageFiles, fileFiles) = splitPresignedUrls(memberId, notice.attachments)

        return NoticeDtoMapper.toDetail(notice, imageFiles, fileFiles)
    }

    @Transactional
    fun delete(noticeId: Long) {
        val notice = noticeRepository.findById(noticeId)
            .orElseThrow { CustomException(NoticeErrorCode.NOTICE_NOT_FOUND) }

        val keys = notice.attachments.map { it.s3Key }.toList()
        if (keys.isNotEmpty()) {
            s3Service.deleteObjectsNow(keys)
        }
        noticeRepository.delete(notice)
    }

    private fun presignGet(memberId: Long, key: String) =
        s3Service.getGetS3Url(memberId = memberId, key = key)

    private fun splitPresignedUrls(
        memberId: Long,
        files: List<NoticeAttachment>
    ): Pair<List<NoticeFileWithUrl>, List<NoticeFileWithUrl>> {
        val (imageFiles, otherFiles) = files.partition { it.attachmentType == UploadOption.IMAGE }

        val imageDtos = imageFiles.map { file ->
            NoticeFileWithUrl(
                id = file.id!!,
                url = presignGet(memberId, file.s3Key).preSignedUrl,
                size = file.attachmentSize
            )
        }

        val fileDtos = otherFiles.map { file ->
            NoticeFileWithUrl(
                id = file.id!!,
                url = presignGet(memberId, file.s3Key).preSignedUrl,
                size = file.attachmentSize
            )
        }

        return imageDtos to fileDtos
    }
}