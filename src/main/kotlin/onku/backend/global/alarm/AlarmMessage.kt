package onku.backend.global.alarm

object AlarmMessage {
    fun kupick(month : Int, status : Boolean): String {
        if(status) {
            return "신청하신 ${month}월 큐픽이 승인되었어요"
        }
        return "신청하신 ${month}월 큐픽이 반려되었어요"
    }
}