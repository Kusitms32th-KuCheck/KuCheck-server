package onku.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import onku.backend.domain.member.enums.ApprovalStatus
import onku.backend.domain.member.enums.Part
import onku.backend.domain.member.enums.Role
import onku.backend.domain.member.enums.SocialType
import onku.backend.global.page.PageResponse

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

    @Schema(description = "권한", example = "USER")
    val role: Role,

    @Schema(description = "운영진 여부", example = "true")
    val isStaff: Boolean,

    @Schema(description = "승인 상태", example = "PENDING")
    val approval: ApprovalStatus,
)

@Schema(description = "회원 페이징 + 상태 별 회원 수 응답")
data class MembersPagedResponse(

    @Schema(description = "승인 대기 중인 회원 수", example = "5")
    val pendingCount: Long,

    @Schema(description = "승인 완료된 회원 수", example = "40")
    val approvedCount: Long,

    @Schema(description = "승인 거절된 회원 수", example = "3")
    val rejectedCount: Long,

    @Schema(description = "APPROVED 상태 회원 페이징 결과")
    val members: PageResponse<MemberItemResponse>,
)

@Schema(description = "운영진(isStaff) 일괄 수정 요청")
data class StaffUpdateRequest(

    @field:NotNull
    @Schema(
        description = "운영진으로 설정할 회원 ID 리스트 (체크박스 선택 결과)",
        example = "[1, 2, 3, 4]"
    )
    val staffMemberIds: List<Long> = emptyList()
)

@Schema(description = "운영진(isStaff) 일괄 수정 결과 응답")
data class StaffUpdateResponse(

    @Schema(
        description = "이번 요청으로 새롭게 운영진이 된 회원 ID 리스트",
        example = "[12, 24, 31, 55]"
    )
    val addedStaffs: List<Long>,

    @Schema(
        description = "이번 요청으로 운영진에서 해제된 회원 ID 리스트",
        example = "[7, 19]"
    )
    val removedStaffs: List<Long>
)

@Schema(description = "운영진 권한(ROLE) 한 건 변경 요청")
data class BulkRoleUpdateItem(

    @field:NotNull
    @Schema(description = "권한을 변경할 회원 ID", example = "1")
    val memberId: Long?,

    @field:NotNull
    @Schema(
        description = "변경할 권한 (GUEST/USER 로의 변경은 허용하지 않음)",
        example = "STAFF"
    )
    val role: Role?
)

@Schema(description = "운영진 권한(ROLE) 일괄 변경 요청")
data class BulkRoleUpdateRequest(

    @field:NotEmpty
    @Schema(
        description = "여러 회원의 권한 변경 요청 리스트",
        example = """[
            {"memberId": 1, "role": "STAFF"},
            {"memberId": 2, "role": "MANAGEMENT"}
        ]"""
    )
    val items: List<BulkRoleUpdateItem>
)