package onku.backend.global.alarm

import onku.backend.domain.absence.enums.AbsenceApprovedType

object AlarmMessage {
    fun kupick(month : Int, status : Boolean): String {
        if(status) {
            return "신청하신 ${month}월 큐픽이 승인되었어요"
        }
        return "신청하신 ${month}월 큐픽이 반려되었어요"
    }

    fun absenceReport(month: Int, day : Int, absenceApprovedType: AbsenceApprovedType) : String {
        return "${month}월 ${day}일 세션 불참사유서가 처리되어 벌점 ${absenceApprovedType.points}점이 기록되었습니다."
    }
}