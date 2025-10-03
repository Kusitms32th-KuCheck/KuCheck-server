package onku.backend.global.exception

class CustomException(
    val errorCode: ApiErrorCode
) : RuntimeException(errorCode.message)