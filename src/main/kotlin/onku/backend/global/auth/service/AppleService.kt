package onku.backend.global.auth.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.JWTClaimsSet
import onku.backend.global.auth.AuthErrorCode
import onku.backend.global.exception.CustomException
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*

@Service
class AppleService {
    private val client = RestClient.create()

    data class AppleTokenResponse(
        val access_token: String?,
        val token_type: String?,
        val expires_in: Long?,
        val refresh_token: String?,
        val id_token: String?
    )

    data class AppleIdTokenPayload(
        val sub: String,
        val email: String?
    )

    fun exchangeCodeForToken(
        code: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String
    ): AppleTokenResponse {
        val form: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("code", code)
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
        }

        return try {
            val res: ResponseEntity<AppleTokenResponse> = client.post()
                .uri("https://appleid.apple.com/auth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(form)
                .retrieve()
                .toEntity(AppleTokenResponse::class.java)

            res.body ?: throw CustomException(AuthErrorCode.APPLE_TOKEN_EMPTY_RESPONSE)
        } catch (e: Exception) {
            throw CustomException(AuthErrorCode.APPLE_API_COMMUNICATION_ERROR)
        }
    }

    fun createClientSecret(
        teamId: String,
        clientId: String,
        keyId: String,
        privateKeyRaw: String
    ): String {
        val now = Instant.now()
        val exp = now.plusSeconds(60 * 5)

        val claims = JWTClaimsSet.Builder()
            .issuer(teamId)
            .subject(clientId)
            .audience("https://appleid.apple.com")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(exp))
            .build()

        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(keyId)
            .build()

        val jwt = SignedJWT(header, claims)
        val ecPrivateKey = parseEcPrivateKey(privateKeyRaw) as ECPrivateKey
        jwt.sign(ECDSASigner(ecPrivateKey))
        return jwt.serialize()
    }

    fun verifyAndParseIdToken(idToken: String, expectedAud: String): AppleIdTokenPayload {
        val jwt = SignedJWT.parse(idToken)
        val kid = jwt.header.keyID ?: throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)

        val jwkSet = try {
            JWKSet.load(java.net.URL("https://appleid.apple.com/auth/keys"))
        } catch (e: Exception) {
            throw CustomException(AuthErrorCode.APPLE_JWKS_FETCH_FAILED)
        }

        val jwk = jwkSet.keys.firstOrNull { it.keyID == kid }
            ?: throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)

        val rsaKey = jwk as? RSAKey ?: throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)
        val publicKey = rsaKey.toRSAPublicKey()

        val verified = jwt.verify(RSASSAVerifier(publicKey))
        if (!verified) throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)

        val claims = jwt.jwtClaimsSet

        if (claims.issuer != "https://appleid.apple.com") {
            throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)
        }

        val audOk = claims.audience?.contains(expectedAud) == true
        if (!audOk) throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)

        val exp = claims.expirationTime ?: throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)
        if (exp.before(Date())) throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)

        val sub = claims.subject ?: throw CustomException(AuthErrorCode.APPLE_ID_TOKEN_INVALID)
        val email = claims.getStringClaim("email")

        return AppleIdTokenPayload(sub = sub, email = email)
    }

    private fun parseEcPrivateKey(raw: String): PrivateKey {
        val pem = raw.trim()
        val base64 = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "\n")
            .replace("\n", "")
            .trim()

        val decoded = Base64.getDecoder().decode(base64)
        val spec = PKCS8EncodedKeySpec(decoded)
        val kf = KeyFactory.getInstance("EC")
        return kf.generatePrivate(spec)
    }
}