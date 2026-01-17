package onku.backend.global.exception

import org.springframework.http.HttpStatus

interface ApiErrorCode {
    val errorCode: String
    val message: String
    val status: HttpStatus
}