package onku.backend.domain.point.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "스터디 점수 수정 결과")
data class StudyPointsResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "7")   val studyPoints: Int
)

@Schema(description = "큐포터즈 점수 수정 결과")
data class KupportersPointsResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "3")   val kupportersPoints: Int
)

@Schema(description = "TF 여부 수정 결과")
data class IsTfResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "true") val isTf: Boolean
)

@Schema(description = "운영진(Staff) 여부 수정 결과")
data class IsStaffResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "false") val isStaff: Boolean
)

@Schema(description = "이번 달 큐픽 승인 상태 결과")
data class KupickApprovalResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "456") val kupickId: Long,
    @Schema(example = "true") val isKupick: Boolean
)

@Schema(description = "메모 수정 결과")
data class MemoResult(
    @Schema(example = "123") val memberId: Long,
    @Schema(example = "운영진 메모 최신 내용") val memo: String?
)
