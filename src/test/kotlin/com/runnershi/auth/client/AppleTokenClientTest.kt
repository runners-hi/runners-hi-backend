package com.runnershi.auth.client

import com.nimbusds.jwt.SignedJWT
import com.runnershi.common.exception.BusinessException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.util.Base64
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AppleTokenClientTest {

    private val restTemplate = mock<RestTemplate>()
    private val privateKey = KeyPairGenerator.getInstance("EC").run {
        initialize(ECGenParameterSpec("secp256r1"))
        Base64.getEncoder().encodeToString(generateKeyPair().private.encoded)
    }

    private fun client(serviceId: String = "com.run.web", redirectUri: String = "https://run.example/cb") =
        AppleTokenClient(restTemplate, "com.run.app", "TEAM", "KEY", privateKey, serviceId, redirectUri)

    @Suppress("UNCHECKED_CAST")
    private fun bodyOf(entity: HttpEntity<*>) = entity.body as MultiValueMap<String, String>

    @Test
    fun `웹 코드 교환은 Service ID와 redirect URI를 보낸다`() {
        whenever(restTemplate.postForEntity(any<String>(), any(), eq(AppleTokenResponse::class.java)))
            .thenReturn(ResponseEntity.ok(AppleTokenResponse(refreshToken = "r")))

        assertEquals("r", client().exchangeWebAuthorizationCode("code"))

        val captor = argumentCaptor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<String>(), captor.capture(), eq(AppleTokenResponse::class.java))
        val body = bodyOf(captor.firstValue)
        assertEquals("com.run.web", body.getFirst("client_id"))
        assertEquals("https://run.example/cb", body.getFirst("redirect_uri"))
        assertEquals("com.run.web", SignedJWT.parse(body.getFirst("client_secret")).jwtClaimsSet.subject)
    }

    @Test
    fun `앱 코드 교환은 번들 ID를 쓰고 redirect URI를 보내지 않는다`() {
        whenever(restTemplate.postForEntity(any<String>(), any(), eq(AppleTokenResponse::class.java)))
            .thenReturn(ResponseEntity.ok(AppleTokenResponse(refreshToken = "r")))

        client().exchangeAuthorizationCode("code")

        val captor = argumentCaptor<HttpEntity<*>>()
        verify(restTemplate).postForEntity(any<String>(), captor.capture(), eq(AppleTokenResponse::class.java))
        val body = bodyOf(captor.firstValue)
        assertEquals("com.run.app", body.getFirst("client_id"))
        assertFalse(body.containsKey("redirect_uri"))
    }

    @Test
    fun `웹 설정이 없으면 코드 교환을 시도하지 않는다`() {
        assertThrows<BusinessException> { client(serviceId = "").exchangeWebAuthorizationCode("code") }
    }

    @Test
    fun `해지는 번들 ID가 실패하면 Service ID로 다시 시도한다`() {
        whenever(restTemplate.postForEntity(any<String>(), any(), eq(String::class.java)))
            .thenThrow(RestClientException("invalid_client"))
            .thenReturn(ResponseEntity.ok(""))

        client().revokeRefreshToken("token")

        val captor = argumentCaptor<HttpEntity<*>>()
        verify(restTemplate, times(2)).postForEntity(any<String>(), captor.capture(), eq(String::class.java))
        assertEquals(listOf("com.run.app", "com.run.web"), captor.allValues.map { bodyOf(it).getFirst("client_id") })
    }

    @Test
    fun `해지가 모두 실패하면 오류를 던진다`() {
        whenever(restTemplate.postForEntity(any<String>(), any(), eq(String::class.java)))
            .thenThrow(RestClientException("invalid_client"))

        assertThrows<BusinessException> { client().revokeRefreshToken("token") }
    }
}
