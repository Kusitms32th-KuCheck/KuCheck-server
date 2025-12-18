package onku.backend.global.crypto.converter;

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import onku.backend.global.context.SpringContext
import onku.backend.global.crypto.PrivacyEncryptor


@Converter
class EncryptedStringConverter : AttributeConverter<String, String> {
    private fun encryptor(): PrivacyEncryptor {
        return SpringContext.getBean(PrivacyEncryptor::class.java)
    }

    override fun convertToDatabaseColumn(raw: String?): String? {
        if (raw == null) return null
        try {
            return encryptor().encrypt(raw)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    override fun convertToEntityAttribute(encrypted: String?): String? {
        if (encrypted == null) return null
        try {
            return encryptor().decrypt(encrypted)
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }
}
