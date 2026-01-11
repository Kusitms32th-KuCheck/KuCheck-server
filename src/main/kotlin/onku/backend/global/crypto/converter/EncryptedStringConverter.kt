package onku.backend.global.crypto.converter;

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import onku.backend.global.context.SpringContext
import onku.backend.global.crypto.PrivacyEncryptor
import onku.backend.global.crypto.exception.DecryptionException
import onku.backend.global.crypto.exception.EncryptionException


@Converter
class EncryptedStringConverter : AttributeConverter<String, String> {
    private fun encryptor(): PrivacyEncryptor {
        return SpringContext.getBean(PrivacyEncryptor::class.java)
    }

    override fun convertToDatabaseColumn(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        try {
            return encryptor().encrypt(raw)
        } catch (e: Exception) {
            throw EncryptionException(e)
        }
    }

    override fun convertToEntityAttribute(encrypted: String?): String? {
        if (encrypted == null) return null
        try {
            return encryptor().decrypt(encrypted)
        } catch (e: Exception) {
            throw DecryptionException(e)
        }
    }
}
