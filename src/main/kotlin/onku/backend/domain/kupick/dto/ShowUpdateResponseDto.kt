package onku.backend.domain.kupick.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.kupick.Kupick
import onku.backend.domain.member.MemberProfile
import onku.backend.domain.member.enums.Part
import java.time.LocalDateTime

data class ShowUpdateResponseDto(
    @Schema(description = "학회원 이름", example = "홍길동")
    val name : String?,
    @Schema(description = "파트", example = "ACKEND, FRONTEND, DESIGN, PLANNING")
    val part : Part,
    @Schema(description = "큐픽id", example = "1")
    val kupickId : Long?,
    @Schema(description = "제출일시", example = "2025-07-15T14:00:00")
    val submitDate : LocalDateTime?,
    @Schema(description = "신청사진Url", example = "https://~")
    val applicationUrl : String?,
    @Schema(description = "시청사진Url", example = "https://~")
    val viewUrl : String?,
    @Schema(description = "승인여부", example = "True")
    val approval : Boolean
    ) {
    companion object {
        fun of(memberProfile : MemberProfile, kupick : Kupick) = ShowUpdateResponseDto (
            memberProfile.name,
            memberProfile.part,
            kupick.id,
            kupick.submitDate,
            kupick.applicationImageUrl,
            kupick.viewImageUrl,
            kupick.approval
        )
    }
}
