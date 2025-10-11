package onku.backend.global.s3.service

import com.amazonaws.HttpMethod
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.Headers
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest
import com.amazonaws.services.s3.model.ResponseHeaderOverrides
import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.s3.dto.GetS3UrlDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URL
import java.util.Date
import java.util.UUID

@Service
class S3Service(
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket : String,
    private val amazonS3Client : AmazonS3
) {
    @Transactional(readOnly = true)
    fun getPostS3Url(memberId: Long, filename: String, folderName : String): GetS3UrlDto {
        // filename 설정하기(profile 경로 + 멤버ID + 랜덤 값)
        val key = "$folderName/$memberId/${UUID.randomUUID()}/$filename"

        // url 유효기간 설정하기(1시간)
        val expiration = getExpiration()

        val contentType = guessContentType(filename)

        // presigned url 생성하기
        val generatePresignedUrlRequest = getPostGeneratePresignedUrlRequest(key, expiration, contentType)
        val url: URL = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest)

        // return
        return GetS3UrlDto(
            preSignedUrl = url.toExternalForm(),
            key = key
        )
    }

    @Transactional(readOnly = true)
    fun getGetS3Url(memberId: Long, key: String): GetS3UrlDto {
        val expiration = getExpiration()
        val contentType = guessContentType(key)

        val generatePresignedUrlRequest = getGetGeneratePresignedUrlRequest(key, expiration, contentType)
        val url: URL = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest)

        return GetS3UrlDto(
            preSignedUrl = url.toExternalForm(),
            key = key
        )
    }

    /** post 용 URL 생성하는 메소드 */
    private fun getPostGeneratePresignedUrlRequest(fileName: String, expiration: Date, contentType : String): GeneratePresignedUrlRequest {
        val request = GeneratePresignedUrlRequest(bucket, fileName)
            .withMethod(HttpMethod.PUT)
            .withKey(fileName)
            .withContentType(contentType)
            .withExpiration(expiration)

        request.addRequestParameter(
            Headers.S3_CANNED_ACL,
            CannedAccessControlList.PublicRead.toString()
        )
        return request
    }

    /** get 용 URL 생성하는 메소드 */
    private fun getGetGeneratePresignedUrlRequest(key: String, expiration: Date, contentType: String): GeneratePresignedUrlRequest {
        val overrides = ResponseHeaderOverrides()
            .withContentType(contentType)
        return GeneratePresignedUrlRequest(bucket, key)
            .withMethod(HttpMethod.GET)
            .withResponseHeaders(overrides)
            .withExpiration(expiration)
    }

    private fun guessContentType(filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".pdf") -> "application/pdf"
            else -> throw CustomException(ErrorCode.INVALID_FILE_EXTENSION)
        }
    }

    companion object {
        /** Presigned URL 만료시간 설정 (기본: 10분) */
        private fun getExpiration(): Date {
            val expiration = Date()
            val expTimeMillis = expiration.time + 1000 * 60 * 10 // 10분
            expiration.time = expTimeMillis
            return expiration
        }
    }


}