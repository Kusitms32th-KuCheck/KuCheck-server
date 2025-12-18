package onku.backend.global.crypto

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class AESService(
    @Value("\${aes.secret-key-base64}")
    secretKeyBase64: String
) : PrivacyEncryptor {

    companion object {
        private const val AES = "AES"
        private const val AES_GCM_NO_PADDING = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12            // bytes
        private const val GCM_TAG_LENGTH_BITS = 128
    }

    private val key: SecretKey =
        Base64.getDecoder().decode(secretKeyBase64).let { keyBytes ->
            require(keyBytes.size == 16 || keyBytes.size == 24 || keyBytes.size == 32) {
                "AES key must be 16/24/32 bytes after Base64 decode."
            }
            SecretKeySpec(keyBytes, AES)
        }

    private val secureRandom = SecureRandom()

    override fun encrypt(raw: String?): String? {
        if (raw == null) return null
        return try {
            val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING).apply {
                init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
            val cipherText = cipher.doFinal(raw.toByteArray(StandardCharsets.UTF_8))
            Base64.getEncoder().encodeToString(iv + cipherText)
        } catch (e: Exception) {
            throw IllegalStateException("AES-GCM encrypt failed", e)
        }
    }

    override fun decrypt(encrypted: String?): String? {
        if (encrypted == null) return null
        return try {
            val decoded = Base64.getDecoder().decode(encrypted)
            require(decoded.size > GCM_IV_LENGTH) { "Invalid encrypted text" }

            val iv = decoded.copyOfRange(0, GCM_IV_LENGTH)
            val cipherText = decoded.copyOfRange(GCM_IV_LENGTH, decoded.size)

            val cipher = Cipher.getInstance(AES_GCM_NO_PADDING).apply {
                init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
            String(cipher.doFinal(cipherText), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalStateException("AES-GCM decrypt failed", e)
        }
    }
}
