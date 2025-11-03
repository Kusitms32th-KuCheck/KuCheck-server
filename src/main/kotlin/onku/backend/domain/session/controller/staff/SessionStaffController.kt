package onku.backend.domain.session.controller.staff

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import onku.backend.domain.member.Member
import onku.backend.domain.session.dto.request.DeleteSessionImageRequest
import onku.backend.domain.session.dto.request.SessionSaveRequest
import onku.backend.domain.session.dto.request.UploadSessionImageRequest
import onku.backend.domain.session.dto.request.UpsertSessionDetailRequest
import onku.backend.domain.session.dto.response.GetDetailSessionResponse
import onku.backend.domain.session.dto.response.GetInitialSessionResponse
import onku.backend.domain.session.dto.response.UploadSessionImageResponse
import onku.backend.domain.session.dto.response.UpsertSessionDetailResponse
import onku.backend.domain.session.facade.SessionFacade
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.page.PageResponse
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/session/staff")
@Tag(name = "[STAFF] 세션 API", description = "세션 관련 API")
class SessionStaffController(
    private val sessionFacade: SessionFacade
) {
    @PostMapping("")
    @Operation(
        summary = "세션 일정 저장",
        description = "세션 일정들을 저장합니다."
    )
    fun sessionSave(
        @RequestBody @Valid sessionSaveRequestList : List<SessionSaveRequest>
    ) : ResponseEntity<SuccessResponse<Boolean>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.sessionSave(sessionSaveRequestList)))
    }

    @GetMapping("")
    @Operation(
        summary = "초기에 저장한 세션정보 불러오기",
        description = "초기에 저장한 세션정보들을 불러옵니다."
    )
    fun getInitialSession(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ) : ResponseEntity<SuccessResponse<PageResponse<GetInitialSessionResponse>>> {
        val safePage = if (page < 1) 0 else page - 1
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.getInitialSession(safePage, size)))
    }

    @PostMapping("/detail")
    @Operation(
        summary = "세션 상세정보 upsert",
        description = "세션 상세정보를 새로 입력하거나 수정합니다."
    )
    fun upsertSessionDetail(
        @RequestBody @Valid upsertSessionDetailRequest : UpsertSessionDetailRequest
    ) : ResponseEntity<SuccessResponse<UpsertSessionDetailResponse>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.upsertSessionDetail(upsertSessionDetailRequest)))
    }

    @PostMapping("/detail/image")
    @Operation(
        summary = "세션 상세정보 이미지 업로드",
        description = "세션의 이미지를 업로드 합니다."
    )
    fun uploadSessionImage(
        @CurrentMember member: Member,
        @RequestBody uploadSessionImageRequest : UploadSessionImageRequest
    ) : ResponseEntity<SuccessResponse<List<UploadSessionImageResponse>>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.uploadSessionImage(member, uploadSessionImageRequest)))
    }

    @DeleteMapping("/detail/image")
    @Operation(
        summary = "세션 상세정보 이미지 삭제",
        description = "세션 상세정보 이미지 삭제를 합니다 (삭제용 presignedUrl 발급)"
    )
    fun deleteSessionImage(
        @RequestBody deleteSessionImageRequest : DeleteSessionImageRequest
    ) : ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.deleteSessionImage(deleteSessionImageRequest)))
    }

    @GetMapping("/detail/{id}")
    @Operation(
        summary = "세션 상세페이지 조회",
        description = "세션 상세정보를 조회합니다."
    )
    fun getSessionDetailPage(
        @PathVariable(name = "id") detailId : Long
    ) : ResponseEntity<SuccessResponse<GetDetailSessionResponse>> {
        return ResponseEntity.ok(SuccessResponse.ok(sessionFacade.getSessionDetailPage(detailId)))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "세션 삭제 [TEMP]",
        description = "세션 ID로 세션과 상세, 연결된 모든 이미지를 삭제합니다. (S3에 저장된 이미지도 즉시 삭제)"
    )
    fun deleteSession(
        @PathVariable id: Long
    ): ResponseEntity<SuccessResponse<Boolean>> {
        sessionFacade.deleteSession(id)
        return ResponseEntity.ok(SuccessResponse.ok(true))
    }
}
