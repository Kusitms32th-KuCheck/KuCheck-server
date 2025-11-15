package onku.backend.domain.notice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import onku.backend.domain.member.Member
import onku.backend.domain.notice.dto.notice.PresignedUploadResponse
import onku.backend.domain.notice.service.NoticeAttachmentService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.enums.UploadOption

@RestController
@RequestMapping("/api/v1/notice/files")
@Tag(
    name = "[STAFF] 공지 파일 업로드 API",
    description = "공지 파일 업로드 관련 API"
)
class NoticeAttachmentController(
    private val noticeAttachmentService: NoticeAttachmentService
) {
    @PostMapping
    @Operation(
        summary = "공지 이미지/파일 업로드 URL 발급 [운영진]",
        description = "filename을 받아 presigned PUT url 발급"
    )
    fun prepareUpload(
        @RequestParam filename: String,
        @RequestParam fileType: UploadOption,
        @CurrentMember member: Member
    ): ResponseEntity<SuccessResponse<PresignedUploadResponse>> {
        val body = noticeAttachmentService.prepareUpload(member, filename, fileType)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}
