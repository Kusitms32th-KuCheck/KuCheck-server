package onku.backend.domain.point.enums

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "점수 종류")
enum class ManualPointType(
    @Schema(description = "각 종류의 활동별 점수 매핑")
    val points: Int
) {
    @Schema(description = "큐픽: 1점")
    KUPICK(1),

    @Schema(description = "TF 활동: 2점")
    TF(2),

    @Schema(description = "운영진 활동: 1점")
    STAFF(1),

    @Schema(description = "스터디: 1점")
    STUDY(1),

    @Schema(description = "큐포터즈: 1점")
    KUPORTERS(1);
}