package onku.backend.global.crypto.exception

open class CryptoException(message: String, cause: Throwable? = null)
    : RuntimeException(message, cause)
class EncryptionException(cause: Throwable? = null) : CryptoException("encryption failed", cause)
class DecryptionException(cause: Throwable? = null) : CryptoException("decryption failed", cause)