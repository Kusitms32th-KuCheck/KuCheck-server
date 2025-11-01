package onku.backend.global.s3.service

import onku.backend.global.exception.CustomException
import onku.backend.global.exception.ErrorCode
import onku.backend.global.s3.dto.GetS3UrlDto
import onku.backend.global.s3.enums.UploadOption
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
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
    private val s3Presigner: S3Presigner,
    private val s3Client: S3Client
) {
    @Transactional(readOnly = true)
    fun getPostS3Url(memberId: Long, filename: String?, folderName : String, option : UploadOption): GetS3UrlDto {
        if(filename.isNullOrBlank()) {
            return GetS3UrlDto(preSignedUrl = "", key = "")
        }

        val key = "$folderName/$memberId/${UUID.randomUUID()}/$filename"
        val contentType = when (option) {
            UploadOption.FILE -> guessFileType(filename)
            UploadOption.IMAGE -> guessImageType(filename)
            else -> throw IllegalArgumentException("Unsupported upload option: $option")
        }


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
    fun getGetS3Url(memberId: Long, key: String?): GetS3UrlDto {
        if(key.isNullOrBlank()) {
            return GetS3UrlDto("", "")
        }
        val contentType = guessFileType(key)

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

    fun deleteObjectsNow(keys: List<String>) {
        val filtered = keys.asSequence().map(String::trim).filter(String::isNotEmpty).toList()
        if (filtered.isEmpty()) return

        filtered.chunked(1000).forEach { chunk ->
            val delete = Delete.builder()
                .quiet(true)
                .objects(chunk.map { ObjectIdentifier.builder().key(it).build() })
                .build()

            val req = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(delete)
                .build()

            val resp = s3Client.deleteObjects(req)
            val errors = resp.errors().orEmpty()

            if (errors.any { it.code() != "NoSuchKey" }) {
                throw CustomException(ErrorCode.SERVER_UNTRACKED_ERROR)
            }
        }
    }

    private fun guessFileType(filename: String): String {
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

    private fun guessImageType(filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".heic") -> "image/heic"
            else -> throw CustomException(ErrorCode.INVALID_FILE_EXTENSION)
        }
    }

    companion object {
        private val DEFAULT_EXPIRE: Duration = Duration.ofMinutes(10)
    }
}