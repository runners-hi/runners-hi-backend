package com.runnershi.domain.terms.controller

import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.terms.dto.TermsResponse
import com.runnershi.domain.terms.entity.ContentType
import com.runnershi.domain.terms.entity.TermsCode
import com.runnershi.domain.terms.service.TermsService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TermsControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var termsService: TermsService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    private val termsList = listOf(
        TermsResponse(1L, TermsCode.TERMS_OF_SERVICE, "서비스 이용약관", ContentType.WEB_URL, "https://example.com/terms", true, 1, false),
        TermsResponse(2L, TermsCode.PRIVACY_POLICY, "개인정보 처리방침", ContentType.WEB_URL, "https://example.com/privacy", true, 1, false),
        TermsResponse(3L, TermsCode.MARKETING, "마케팅 수신 동의", ContentType.NONE, null, false, 1, false)
    )

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("GET /api/terms")
    inner class GetTermsList {

        @Test
        @DisplayName("비로그인 유저 - 약관 목록 조회 성공")
        fun withoutAuth() {
            whenever(termsService.getTermsList(isNull())).thenReturn(termsList)

            mockMvc.perform(get("/api/terms"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].title").value("서비스 이용약관"))
                .andExpect(jsonPath("$.data[0].required").value(true))
        }

        @Test
        @DisplayName("로그인 유저 - 동의 여부 포함 조회")
        fun withAuth() {
            val agreedTermsList = termsList.mapIndexed { index, terms ->
                if (index == 0) terms.copy(agreed = true) else terms
            }
            whenever(termsService.getTermsList(any())).thenReturn(agreedTermsList)

            mockMvc.perform(
                get("/api/terms")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data[0].agreed").value(true))
                .andExpect(jsonPath("$.data[1].agreed").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/terms/agree")
    inner class AgreeToTerms {

        @Test
        @DisplayName("약관 동의 성공")
        fun success() {
            mockMvc.perform(
                post("/api/terms/agree")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "agreements" to listOf(
                            mapOf("termsId" to 1, "agreed" to true),
                            mapOf("termsId" to 2, "agreed" to true)
                        )
                    )))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("인증 없이 약관 동의 시 에러 반환")
        fun withoutAuth_returnsError() {
            mockMvc.perform(
                post("/api/terms/agree")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "agreements" to listOf(mapOf("termsId" to 1, "agreed" to true))
                    )))
            )
                .andExpect(status().isForbidden)
        }
    }
}
