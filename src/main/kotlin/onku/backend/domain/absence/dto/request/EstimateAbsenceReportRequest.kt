package onku.backend.domain.absence.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import onku.backend.domain.absence.enums.AbsenceApprovedType

data class EstimateAbsenceReportRequest(
    @Schema(description = "승인 타입", example = "EXCUSED / ABSENT / ABSENT_WITH_DOC / ABSENT_WITH_CAUSE / LATE / EARLY_LEAVE")
    val approvedType: AbsenceApprovedType
)
