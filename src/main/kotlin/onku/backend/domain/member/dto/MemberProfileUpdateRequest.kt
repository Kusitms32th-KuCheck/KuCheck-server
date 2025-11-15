package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.member.enums.Part

@Schema(description = "프로필 기본 정보 수정 요청")
data class MemberProfileUpdateRequest(

    @Schema(description = "이름", example = "김온쿠")
    val name: String,

    @Schema(description = "학교명", example = "한국대학교")
    val school: String? = null,

    @Schema(description = "전공", example = "컴퓨터공학과")
    val major: String? = null,

    @Schema(description = "파트", example = "SERVER")
    val part: Part,

    @Schema(description = "전화번호", example = "010-1234-5678")
    val phoneNumber: String? = null,
)