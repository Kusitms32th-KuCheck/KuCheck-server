package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.member.enums.Part

@Schema(description = "내 프로필 요약 응답")
data class MemberProfileResponse(

    @field:Schema(
        description = "이메일",
        example = "onku@example.com"
    )
    val email: String?,

    @field:Schema(
        description = "이름",
        example = "김온쿠"
    )
    val name: String?,

    @field:Schema(description = "파트", example = "SERVER")
    val part: Part,

    @field:Schema(description = "상벌점 총합", example = "15")
    val totalPoints: Long,

    @field:Schema(description = "프로필 이미지 URL", example = "https://.../profile.png")
    val profileImage: String?
)
