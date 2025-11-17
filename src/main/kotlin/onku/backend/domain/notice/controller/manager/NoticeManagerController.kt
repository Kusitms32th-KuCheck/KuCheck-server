package onku.backend.domain.notice.controller.manager

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import onku.backend.domain.member.Member
import onku.backend.domain.notice.dto.notice.NoticeCreateRequest
import onku.backend.domain.notice.dto.notice.NoticeDetailResponse
import onku.backend.domain.notice.dto.notice.NoticeUpdateRequest
import onku.backend.domain.notice.dto.notice.PresignedUploadResponse
import onku.backend.domain.notice.service.NoticeAttachmentService
import onku.backend.domain.notice.service.NoticeService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.enums.UploadOption

@RestController
@RequestMapping("/api/v1/notice/manage")
@Tag(
    name = "[STAFF] 공지 관리 API",
    description = "공지 생성/수정/삭제/상세 조회 API"
)
class NoticeManagerController(
    private val noticeService: NoticeService,
    private val noticeAttachmentService: NoticeAttachmentService
) {

    @PostMapping
    @Operation(
        summary = "공지 등록",
        description = "제목, 카테고리, 내용, 이미지, pdf로 등록"
    )
    fun create(
        @CurrentMember member: Member,
        @RequestBody @Valid req: NoticeCreateRequest
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>> {
        val body = noticeService.create(member, req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PostMapping("files")
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

    @PutMapping("/{noticeId}")
    @Operation(
        summary = "공지 수정",
        description = "등록과 동일한 request/response"
    )
    fun update(
        @PathVariable noticeId: Long,
        @CurrentMember member: Member,
        @RequestBody @Valid req: NoticeUpdateRequest
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>> {
        val body = noticeService.update(noticeId, member, req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @DeleteMapping("/{noticeId}")
    @Operation(
        summary = "공지 삭제",
        description = "공지 삭제"
    )
    fun delete(@PathVariable noticeId: Long): ResponseEntity<SuccessResponse<Unit>> {
        noticeService.delete(noticeId)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }
}
