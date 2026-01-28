package com.runnershi.common.security

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    companion object {
        const val TOKEN_TYPE_ACCESS = "ACCESS"
        const val TOKEN_TYPE_REFRESH = "REFRESH"
        const val CLAIM_TYPE = "type"
    }

    fun createAccessToken(userId: Long): String {
        return createToken(userId, TOKEN_TYPE_ACCESS, jwtProperties.accessTokenValidity)
    }

    fun createRefreshToken(userId: Long): String {
        return createToken(userId, TOKEN_TYPE_REFRESH, jwtProperties.refreshTokenValidity)
    }

    fun getRefreshTokenValidity(): Long = jwtProperties.refreshTokenValidity

    private fun createToken(userId: Long, tokenType: String, validity: Long): String {
        val now = Date()
        val expiration = Date(now.time + validity)

        return Jwts.builder()
            .subject(userId.toString())
            .claim(CLAIM_TYPE, tokenType)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long {
        val claims = parseToken(token)
        return claims.subject.toLong()
    }

    fun getTokenType(token: String): String {
        val claims = parseToken(token)
        return claims[CLAIM_TYPE, String::class.java] ?: TOKEN_TYPE_ACCESS
    }

    fun validateAccessToken(token: String) {
        val tokenType = getTokenType(token)
        if (tokenType != TOKEN_TYPE_ACCESS) {
            throw BusinessException(ErrorCode.ACCESS_TOKEN_REQUIRED)
        }
    }

    private fun parseToken(token: String): io.jsonwebtoken.Claims {
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: ExpiredJwtException) {
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        } catch (e: MalformedJwtException) {
            throw BusinessException(ErrorCode.MALFORMED_TOKEN)
        } catch (e: SignatureException) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        } catch (e: JwtException) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseToken(token)
            true
        } catch (e: BusinessException) {
            false
        }
    }
}
