package com.runnershi.auth.client

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.security.KeyFactory
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.Date

@Component
class AppleTokenClient(
    private val restTemplate: RestTemplate,
    @Value("\${oauth.apple.bundle-id}") private val clientId: String,
    @Value("\${oauth.apple.team-id}") private val teamId: String,
    @Value("\${oauth.apple.key-id}") private val keyId: String,
    @Value("\${oauth.apple.private-key}") private val privateKey: String
) {

    companion object {
        private const val APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token"
        private const val APPLE_AUDIENCE = "https://appleid.apple.com"
    }

    fun exchangeAuthorizationCode(authorizationCode: String): String {
        validateConfig()

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        return try {
            val body = LinkedMultiValueMap<String, String>().apply {
                add("client_id", clientId)
                add("client_secret", createClientSecret())
                add("code", authorizationCode)
                add("grant_type", "authorization_code")
            }

            val response = restTemplate.postForEntity(
                APPLE_TOKEN_URL,
                HttpEntity(body, headers),
                AppleTokenResponse::class.java
            ).body ?: throw BusinessException(ErrorCode.INVALID_TOKEN, "Apple token 응답이 비어 있습니다")

            if (!response.error.isNullOrBlank()) {
                throw BusinessException(
                    ErrorCode.INVALID_TOKEN,
                    "Apple token 교환 실패: ${response.errorDescription ?: response.error}"
                )
            }

            response.refreshToken
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN, "Apple refresh token이 없습니다")
        } catch (e: BusinessException) {
            throw e
        } catch (e: RestClientException) {
            throw BusinessException(ErrorCode.INVALID_TOKEN, "Apple token 교환 실패: ${e.message}")
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Apple client secret 생성 실패: ${e.message}")
        }
    }

    private fun validateConfig() {
        if (clientId.isBlank() || teamId.isBlank() || keyId.isBlank() || privateKey.isBlank()) {
            throw BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Apple OAuth 설정이 누락되었습니다")
        }
    }

    private fun createClientSecret(): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .issuer(teamId)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plus(180, ChronoUnit.DAYS)))
            .audience(APPLE_AUDIENCE)
            .subject(clientId)
            .build()

        val signedJwt = SignedJWT(
            JWSHeader.Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .keyID(keyId)
                .build(),
            claims
        )
        signedJwt.sign(ECDSASigner(loadPrivateKey()))
        return signedJwt.serialize()
    }

    private fun loadPrivateKey(): ECPrivateKey {
        val normalizedPem = privateKey
            .replace("\\n", "\n")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s".toRegex(), "")

        val decodedKey = Base64.getDecoder().decode(normalizedPem)
        val keySpec = PKCS8EncodedKeySpec(decodedKey)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePrivate(keySpec) as ECPrivateKey
    }
}

data class AppleTokenResponse(
    @JsonProperty("refresh_token")
    val refreshToken: String? = null,
    val error: String? = null,
    @JsonProperty("error_description")
    val errorDescription: String? = null
)
