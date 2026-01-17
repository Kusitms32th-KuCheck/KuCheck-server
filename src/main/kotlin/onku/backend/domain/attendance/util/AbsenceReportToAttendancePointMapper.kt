package onku.backend.domain.attendance.util

import onku.backend.domain.absence.enums.AbsenceReportApproval
import onku.backend.domain.absence.enums.AbsenceApprovedType
import onku.backend.domain.attendance.enums.AttendancePointType

/**
 * AbsenceReport의 승인 상태(approval)와 유형(submitType)을
 * AttendancePointType으로 매핑한다.
 *
 * 사용 예)
 * val type = AbsenceReportToAttendancePointMapper.map(approval, submitType)  // AttendancePointType
 * val points = type.points                                 // Int (각 상태의 점수)
 */
object AbsenceReportToAttendancePointMapper {

    /**
     * - 기획 요구사항 정리
     * - SUBMIT(제출만)인 경우: ABSENT로 간주 (-3점)
     * - APPROVED(승인)인 경우: status에 따라 매핑
     * - 그 외: ABSENT
     */
    fun map(
        approval: AbsenceReportApproval,
        status: AbsenceApprovedType
    ): AttendancePointType {
        return when (approval) {
            AbsenceReportApproval.SUBMIT -> AttendancePointType.ABSENT
            AbsenceReportApproval.APPROVED -> when (status) {
                AbsenceApprovedType.EXCUSED -> AttendancePointType.EXCUSED
                AbsenceApprovedType.ABSENT -> AttendancePointType.ABSENT
                AbsenceApprovedType.ABSENT_WITH_DOC -> AttendancePointType.ABSENT_WITH_DOC
                AbsenceApprovedType.ABSENT_WITH_CAUSE -> AttendancePointType.ABSENT_WITH_CAUSE
                AbsenceApprovedType.LATE -> AttendancePointType.LATE
                AbsenceApprovedType.EARLY_LEAVE -> AttendancePointType.EARLY_LEAVE
            }
            else -> AttendancePointType.ABSENT
        }
    }
}