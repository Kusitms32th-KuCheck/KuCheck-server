package onku.backend.domain.notice.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import onku.backend.domain.notice.dto.category.*
import onku.backend.domain.notice.service.NoticeCategoryService
import onku.backend.global.response.SuccessResponse

@RestController
@RequestMapping("/api/v1/notice/categories")
@Tag(
    name = "[STAFF] 공지 카테고리 API",
    description = "공지 카테고리 관련 API"
)
class NoticeCategoryController(
    private val noticeCategoryService: NoticeCategoryService
) {

    @GetMapping
    @Operation(summary = "카테고리 조회 [운영진]", description = "{id, name, color} 목록 반환")
    fun list(): ResponseEntity<SuccessResponse<List<NoticeCategoryResponse>>> {
        val body = noticeCategoryService.list()
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @GetMapping("/colors/available")
    @Operation(summary = "사용 가능한 카테고리 색상 조회 [운영진]", description = "아직 사용되지 않은 색상 리스트 반환")
    fun availableColors(): ResponseEntity<SuccessResponse<AvailableColorsResponse>> {
        val body = noticeCategoryService.availableColors()
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PostMapping
    @Operation(
        summary = "카테고리 등록 [운영진]",
        description = "같은 이름 불가, 이름 7자 이상 불가, 색상 중복 불가"
    )
    fun create(
        @RequestBody @Valid req: NoticeCategoryCreateRequest
    ): ResponseEntity<SuccessResponse<NoticeCategoryResponse>> {
        val body = noticeCategoryService.create(req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정 [운영진]", description = "이름/색상 수정. 제약 동일")
    fun update(
        @PathVariable categoryId: Long,
        @RequestBody @Valid req: NoticeCategoryUpdateRequest
    ): ResponseEntity<SuccessResponse<NoticeCategoryResponse>> {
        val body = noticeCategoryService.update(categoryId, req)
        return ResponseEntity.ok(SuccessResponse.ok(body))
    }

    @DeleteMapping("/{categoryId}")
    @Operation(
        summary = "카테고리 삭제 [운영진]",
        description = "해당 카테고리로 작성된 공지가 있으면 삭제 불가"
    )
    fun delete(@PathVariable categoryId: Long): ResponseEntity<SuccessResponse<Unit>> {
        noticeCategoryService.delete(categoryId)
        return ResponseEntity.ok(SuccessResponse.ok(Unit))
    }
}
