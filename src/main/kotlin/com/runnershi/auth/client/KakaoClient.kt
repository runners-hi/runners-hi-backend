package com.runnershi.auth.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

@Component
class KakaoClient(
    private val restTemplate: RestTemplate
) : AuthClient {

    companion object {
        private const val KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me"
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
