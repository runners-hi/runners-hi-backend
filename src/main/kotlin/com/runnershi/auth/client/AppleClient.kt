package com.runnershi.auth.client

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI

@Component
class AppleClient(
    @Value("\${oauth.apple.bundle-id}") private val bundleId: String
) : AuthClient {

    companion object {
        private const val APPLE_JWKS_URL = "https://appleid.apple.com/auth/keys"
        private const val APPLE_ISSUER = "https://appleid.apple.com"
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
