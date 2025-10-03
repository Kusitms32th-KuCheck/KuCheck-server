package onku.backend.global.response.result

enum class ResponseState(
    val code: Int,
    val message: String
) {
    SUCCESS(1, "성공하였습니다."),
    FAIL(-1, "실패하였습니다.");
}
