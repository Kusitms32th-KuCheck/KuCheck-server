package onku.backend.global.crypto

interface PrivacyEncryptor {
    @Throws(Exception::class)
    fun encrypt(raw: String?): String?
    @Throws(Exception::class)
    fun decrypt(encrypted: String?): String?
}