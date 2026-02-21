package com.runnershi.auth.client

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GoogleClient(
    @Value("\${oauth.google.client-id}") private val clientId: String
) : AuthClient {

    private val log = LoggerFactory.getLogger(javaClass)

    private val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(
        NetHttpTransport(),
        GsonFactory.getDefaultInstance()
    )
        .setAudience(listOf(clientId))
        .build()

    override fun getProvider(): Provider = Provider.GOOGLE

    override fun verify(token: String): UserInfo {
        val idToken = try {
            verifier.verify(token)
        } catch (e: Exception) {
            throw BusinessException(ErrorCode.INVALID_TOKEN, "Google id_token 검증 실패: ${e.message}")
        } ?: throw BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 Google id_token입니다")

        val payload = idToken.payload

        return UserInfo(
            providerId = payload.subject,
            email = payload.email,
            nickname = payload["name"] as? String
        )
    }

    /**
     * Google OAuth 연결 해제.
     * 현재 서버에 access token을 저장하지 않으므로 revoke 불가 — 로그만 남기고 skip.
     */
    fun revokeToken(providerId: String) {
        log.info("Google revoke 생략: access token 미저장 (providerId={})", providerId)
    }
}
