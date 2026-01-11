package onku.backend.crypto

import onku.backend.global.crypto.AESService
import onku.backend.global.crypto.exception.DecryptionException
import org.junit.jupiter.api.Assertions.assertThrows
import java.util.*
import kotlin.test.Test

class CryptoErrorTest {
    private fun keyA(): String =
        Base64.getEncoder().encodeToString(ByteArray(32) { 1 })

    private fun keyB(): String =
        Base64.getEncoder().encodeToString(ByteArray(32) { 2 })

    @Test
    fun `decrypt - invalid base64 throws DecryptionException`() {
        val aes = AESService(keyA())

        assertThrows(DecryptionException::class.java) {
            aes.decrypt("%%%not-base64%%%")
        }
    }

    @Test
    fun `decrypt - too short ciphertext throws DecryptionException`() {
        val aes = AESService(keyA())

        val tooShort = Base64.getEncoder().encodeToString(byteArrayOf(0x01))
        assertThrows(DecryptionException::class.java) {
            aes.decrypt(tooShort)
        }
    }

    @Test
    fun `decrypt - wrong key throws DecryptionException`() {
        val encryptor = AESService(keyA())
        val decryptor = AESService(keyB())

        val cipher = encryptor.encrypt("hello")!!

        assertThrows(DecryptionException::class.java) {
            decryptor.decrypt(cipher)
        }
    }
}