package onku.backend.global.s3.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import onku.backend.domain.member.Member
import onku.backend.global.annotation.CurrentMember
import onku.backend.global.response.SuccessResponse
import onku.backend.global.s3.dto.GetS3UrlDto
import onku.backend.global.s3.enums.UploadOption
import onku.backend.global.s3.service.S3Service
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "S3 API", description = "Presigned Url 발급 API")
@RestController
@RequestMapping("/api/v1/s3")
class S3Controller(
    private val s3Service : S3Service
) {
    /**
     * Todo 테스트용 컨트롤러임 나중에 지우기
     */
    @GetMapping("/postUrl")
    @Operation(summary = "업로드 용 postUrl", description = "업로드 용 PresignedUrl을 반환합니다.")
    fun postUrl(@CurrentMember member : Member, folderName : String, fileName : String) : ResponseEntity<SuccessResponse<GetS3UrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(s3Service.getPostS3Url(member.id!!, fileName, folderName, UploadOption.FILE)))
    }

    @GetMapping("/getUrl")
    @Operation(summary = "조회 용 getUrl", description = "조회 용 PresignedUrl을 반환합니다.")
    fun getUrl(@CurrentMember member : Member, keyName: String) : ResponseEntity<SuccessResponse<GetS3UrlDto>> {
        return ResponseEntity.ok(SuccessResponse.ok(s3Service.getGetS3Url(member.id!!, keyName)))
    }
}