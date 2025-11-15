package onku.backend.domain.notice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import onku.backend.domain.member.Member
import onku.backend.domain.notice.dto.notice.*
import onku.backend.domain.notice.service.NoticeService
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse

@RestController
@RequestMapping("/api/v1/notice")
@Tag(
    name = "[STAFF] 공지 API",
    description = "공지 관련 API"
)
class NoticeController(
    private val noticeService: NoticeService
) {

    @GetMapping
    @Operation(
        summary = "공지 리스트 조회 [운영진]",
        description = "id, 제목, 작성자id, 작성자이름, {카테고리 이름/색}, 작성일(YYYY/MM/DD HH:MM), 상태, 이미지, 파일을 페이징하여 반환. 전체 조회가 필요한 경우 카테고리 id는 비워두시면 됩니다."
    )
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) categoryId: Long?,
        @CurrentMember member: Member
    ): ResponseEntity<SuccessResponse<PageResponse<NoticeListItemResponse>>> {
        val safePage = if (page < 1) 0 else page - 1
        val body = noticeService.list(member, safePage, size, categoryId)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @GetMapping("/{noticeId}")
    @Operation(
        summary = "공지 단일 조회 [운영진]",
        description = "제목, 카테고리, 작성일자(MM:dd HH:MM), 내용, 작성자id, 작성자이름, 이미지, 파일 presigned url 리스트"
    )
    fun get(
        @PathVariable noticeId: Long,
        @CurrentMember member: Member
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>> {
        val body = noticeService.get(noticeId, member)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PostMapping
    @Operation(
        summary = "공지 등록 [운영진]",
        description = "제목, 카테고리, 내용, 이미지, pdf로 등록"
    )
    fun create(
        @CurrentMember member: Member,
        @RequestBody @Valid req: NoticeCreateRequest
    ): ResponseEntity<SuccessResponse<NoticeDetailResponse>> {
        val body = noticeService.create(member, req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PutMapping("/{noticeId}")
    @Operation(
        summary = "공지 수정 [운영진]",
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
        summary = "공지 삭제 [운영진]",
        description = "공지 삭제"
    )
    fun delete(@PathVariable noticeId: Long): ResponseEntity<SuccessResponse<Unit>> {
        noticeService.delete(noticeId)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }

    @GetMapping("/search")
    @Operation(
        summary = "공지 검색 [운영진]",
        description = "검색어로 공지 제목/내용에서 검색하여 페이징 반환합니다."
    )
    fun search(
        @RequestParam keyword: String,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<SuccessResponse<PageResponse<NoticeListItemResponse>>> {
        val safePage = if (page < 1) 0 else page - 1
        val body = noticeService.search(keyword, safePage, size)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }
}