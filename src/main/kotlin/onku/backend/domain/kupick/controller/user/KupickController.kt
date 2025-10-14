package onku.backend.domain.kupick.controller.user

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.kupick.dto.ViewMyKupickResponseDto
import onku.backend.domain.kupick.facade.KupickFacade
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetPreSignedUrlDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/kupick")
@Tag(name = "큐픽 API", description = "큐픽 관련 CRUD API")
class KupickController(
    private val kupickFacade: KupickFacade
) {
    @GetMapping("/application")
    @Operation(
        summary = "큐픽 신청 서류 제출",
        description = "큐픽 신청용 서류 제출 signedUrl 반환"
    )
    fun submitApplication(
        @CurrentMember member : Member, fileName : String
    ) : ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(kupickFacade.submitApplication(member, fileName)))
    }

    @GetMapping("/view")
    @Operation(
        summary = "큐픽 시청 서류 제출",
        description = "큐픽 시청 증빙 서류 제출 signedUrl 반환"
    )
    fun submitView(
        @CurrentMember member: Member, fileName: String
    ) : ResponseEntity<SuccessResponse<GetPreSignedUrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(kupickFacade.submitView(member, fileName)))
    }

    @GetMapping("/my")
    @Operation(
        summary = "큐픽 조회",
        description = "내가 신청한 큐픽 내역 조회"
    )
    fun viewMyKupick(
        @CurrentMember member: Member
    ) : ResponseEntity<SuccessResponse<ViewMyKupickResponseDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(kupickFacade.viewMyKupick(member)))
    }
}