package com.runnershi.auth.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.Date

@Component
class AppleClient(
    @Value("\${oauth.apple.bundle-id}") private val bundleId: String,
    @Value("\${oauth.apple.team-id:}") private val teamId: String,
    @Value("\${oauth.apple.key-id:}") private val keyId: String,
    @Value("\${oauth.apple.private-key:}") private val privateKey: String,
    private val restTemplate: RestTemplate
) : AuthClient {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISSUER = "https://appleid.apple.com"
        private const val APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token"
        private const val CLIENT_SECRET_VALIDITY_MS = 5 * 60 * 1000L // 5분
    }

    override fun getProvider(): Provider = Provider.APPLE

    override fun verify(token: String): UserInfo {
        val claims = verifyToken(token)

        return UserInfo(
            providerId = claims.subject,
            email = claims.getStringClaim("email"),
            nickname = null
        )
    }

    fun exchangeCodeForRefreshToken(authorizationCode: String): String? {
        if (teamId.isBlank() || keyId.isBlank() || privateKey.isBlank()) {
            log.warn("Apple OAuth 설정이 누락되어 refresh token 교환을 건너뜁니다")
            return null
        }

        try {
            val clientSecret = generateClientSecret()

            val params = LinkedMultiValueMap<String, String>().apply {
                add("client_id", bundleId)
                add("client_secret", clientSecret)
                add("code", authorizationCode)
                add("grant_type", "authorization_code")
            }

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val response = restTemplate.postForObject(
                APPLE_TOKEN_URL,
                HttpEntity(params, headers),
                Map::class.java
            )

            val refreshToken = response?.get("refresh_token") as? String
            if (refreshToken == null) {
                log.warn("Apple token 응답에 refresh_token이 없습니다: {}", response)
            }
            return refreshToken
        } catch (e: RestClientException) {
            log.error("Apple token 교환 API 호출 실패: {}", e.message)
            return null
        } catch (e: Exception) {
            log.error("Apple refresh token 교환 실패: {}", e.message)
            return null
        }
    }

    private fun generateClientSecret(): String {
        val now = Date()
        val expiration = Date(now.time + CLIENT_SECRET_VALIDITY_MS)

        val ecKey = ECKey.parseFromPEMEncodedObjects(privateKey) as ECKey

        val header = JWSHeader.Builder(JWSAlgorithm.ES256)
            .keyID(keyId)
            .build()

        val claims = JWTClaimsSet.Builder()
            .issuer(teamId)
            .audience(APPLE_ISSUER)
            .subject(bundleId)
            .issueTime(now)
            .expirationTime(expiration)
            .build()

        val jwt = SignedJWT(header, claims)
        jwt.sign(ECDSASigner(ecKey))

        return jwt.serialize()
    }

    private fun verifyToken(token: String): JWTClaimsSet {
        try {
            val jwkSet = JWKSet.load(URI(APPLE_JWKS_URL).toURL())

            val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {
                jwsKeySelector = JWSVerificationKeySelector(
                    JWSAlgorithm.RS256,
                    ImmutableJWKSet(jwkSet)
                )
                jwtClaimsSetVerifier = DefaultJWTClaimsVerifier<SecurityContext>(
                    JWTClaimsSet.Builder()
                        .issuer(APPLE_ISSUER)
                        .audience(bundleId)
                        .build(),
                    setOf("sub", "iss", "aud", "exp")
                )
            }

            return jwtProcessor.process(token, null)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN, "Apple id_token 검증 실패: ${e.message}")
        }
    }
}
