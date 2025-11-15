package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.enums.SocialType

@Schema(description = "회원 승인/가입 현황 리스트 응답")
data class MemberInfoListResponse(

    @Schema(description = "승인 대기 중인 회원 수", example = "5")
    val pendingCount: Long,

    @Schema(description = "승인 완료된 회원 수", example = "40")
    val approvedCount: Long,

    @Schema(description = "승인 거절된 회원 수", example = "3")
    val rejectedCount: Long,

    @Schema(description = "회원 정보 리스트")
    val members: List<MemberItemResponse>,
)

@Schema(description = "회원 한 명에 대한 기본/승인 정보")
data class MemberItemResponse(

    @Schema(description = "회원 ID", example = "1")
    val memberId: Long,

    @Schema(description = "이름", example = "김온쿠")
    val name: String?,

    @Schema(description = "프로필 이미지 URL", example = "https://cdn.onku.kr/profile/1.png")
    val profileImageUrl: String?,

    @Schema(description = "파트", example = "SERVER")
    val part: Part,

    @Schema(description = "학교명", example = "한국대학교")
    val school: String?,

    @Schema(description = "전공", example = "컴퓨터공학과")
    val major: String?,

    @Schema(description = "전화번호", example = "010-1234-5678")
    val phoneNumber: String?,

    @Schema(description = "소셜 로그인 타입", example = "GOOGLE")
    val socialType: SocialType,

    @Schema(description = "이메일", example = "onku@example.com")
    val email: String?,

    @Schema(description = "승인 상태", example = "PENDING")
    val approval: ApprovalStatus,
)

@Schema(description = "운영진용 회원 승인 현황 리스트 응답")
data class MemberApprovalListResponse(

    @Schema(description = "승인 대기 중인 회원 수", example = "5")
    val pendingCount: Long,

    @Schema(description = "승인 완료된 회원 수", example = "40")
    val approvedCount: Long,

    @Schema(description = "승인 거절된 회원 수", example = "3")
    val rejectedCount: Long,

    @Schema(description = "회원 승인 정보 리스트")
    val members: List<MemberItemResponse>,
)