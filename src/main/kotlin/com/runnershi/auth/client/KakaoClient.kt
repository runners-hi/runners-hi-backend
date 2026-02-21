package com.runnershi.auth.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class KakaoClient(
    private val restTemplate: RestTemplate,
    @Value("\${oauth.kakao.admin-key}") private val adminKey: String
) : AuthClient {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
        private const val KAKAO_UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink"
    }

    override fun getProvider(): Provider = Provider.KAKAO

    override fun verify(token: String): UserInfo {
        val headers = HttpHeaders().apply {
            setBearerAuth(token)
        }

        return try {
            val response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                KakaoUserResponse::class.java
            )

            val kakaoUser = response.body
                ?: throw BusinessException(ErrorCode.INVALID_TOKEN)

            UserInfo(
                providerId = kakaoUser.id.toString(),
                email = kakaoUser.kakaoAccount?.email,
                nickname = kakaoUser.kakaoAccount?.profile?.nickname
            )
        } catch (e: RestClientException) {
            throw BusinessException(ErrorCode.INVALID_TOKEN)
        }
    }

    /**
     * Kakao 연결 해제 (Admin Key 방식).
     * @param providerId Kakao 유저 ID (숫자 문자열)
     */
    fun unlinkUser(providerId: String) {
        val headers = HttpHeaders().apply {
            set("Authorization", "KakaoAK $adminKey")
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val body = LinkedMultiValueMap<String, String>().apply {
            add("target_id_type", "user_id")
            add("target_id", providerId)
        }

        try {
            restTemplate.postForEntity(
                KAKAO_UNLINK_URL,
                HttpEntity(body, headers),
                String::class.java
            )
            log.info("Kakao unlink 성공: providerId={}", providerId)
        } catch (e: RestClientException) {
            log.warn("Kakao unlink 실패 (탈퇴는 계속 진행): providerId={}, error={}", providerId, e.message)
        }
    }
}

data class KakaoUserResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?
) {
    data class KakaoAccount(
        val email: String?,
        val profile: Profile?
    ) {
        data class Profile(
            val nickname: String?
        )
    }
}
