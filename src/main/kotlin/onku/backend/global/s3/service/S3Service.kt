package onku.backend.global.s3.service

import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.s3.dto.GetS3UrlDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URL
import java.time.Duration
import java.util.UUID

@Service
class S3Service(
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket : String,
    private val s3Presigner: S3Presigner
) {
    @Transactional(readOnly = true)
    fun getPostS3Url(memberId: Long, filename: String, folderName : String): GetS3UrlDto {
        val key = "$folderName/$memberId/${UUID.randomUUID()}/$filename"
        val contentType = guessContentType(filename)

        val putObjReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .build()

        val presignReq = PutObjectPresignRequest.builder()
            .signatureDuration(DEFAULT_EXPIRE)
            .putObjectRequest(putObjReq)
            .build()

        val presigned = s3Presigner.presignPutObject(presignReq)
        val url: URL = presigned.url()

        return GetS3UrlDto(preSignedUrl = url.toExternalForm(), key = key)
    }

    @Transactional(readOnly = true)
    fun getGetS3Url(memberId: Long, key: String): GetS3UrlDto {
        val contentType = guessContentType(key)

        // 응답 Content-Type을 강제로 지정하고 싶으면 responseContentType 사용
        val getObjReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .responseContentType(contentType)
            .build()

        val presignReq = GetObjectPresignRequest.builder()
            .signatureDuration(DEFAULT_EXPIRE)
            .getObjectRequest(getObjReq)
            .build()

        val presigned = s3Presigner.presignGetObject(presignReq)
        val url: URL = presigned.url()

        return GetS3UrlDto(preSignedUrl = url.toExternalForm(), key = key)
    }

    @Transactional(readOnly = true)
    fun getDeleteS3Url(key: String): GetS3UrlDto {
        val deleteReq = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignReq = DeleteObjectPresignRequest.builder()
            .signatureDuration(DEFAULT_EXPIRE)
            .deleteObjectRequest(deleteReq)
            .build()

        val presigned = s3Presigner.presignDeleteObject(presignReq)
        val url: URL = presigned.url()

        return GetS3UrlDto(preSignedUrl = url.toExternalForm(), key = key)
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
        private val DEFAULT_EXPIRE: Duration = Duration.ofMinutes(10)
    }


}