package com.runnershi.domain.auth.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.auth.dto.AuthResponse
import com.runnershi.domain.auth.dto.TokenResponse
import com.runnershi.domain.auth.service.AuthService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Nested
    @DisplayName("POST /api/auth/kakao")
    inner class KakaoLogin {

        @Test
        @DisplayName("유효한 카카오 토큰으로 로그인 성공")
        fun success() {
            val authResponse = AuthResponse("access-token", "refresh-token", true)
            whenever(authService.loginWithKakao(any())).thenReturn(authResponse)

            mockMvc.perform(
                post("/api/auth/kakao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("accessToken" to "kakao-token")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.isNewUser").value(true))
        }

        @Test
        @DisplayName("accessToken이 빈 값이면 400 반환")
        fun blankToken_returns400() {
            mockMvc.perform(
                post("/api/auth/kakao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("accessToken" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/auth/google")
    inner class GoogleLogin {

        @Test
        @DisplayName("유효한 구글 토큰으로 로그인 성공")
        fun success() {
            val authResponse = AuthResponse("access-token", "refresh-token", false)
            whenever(authService.loginWithGoogle(any())).thenReturn(authResponse)

            mockMvc.perform(
                post("/api/auth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("idToken" to "google-token")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        }
    }

    @Nested
    @DisplayName("POST /api/auth/apple")
    inner class AppleLogin {

        @Test
        @DisplayName("유효한 애플 토큰으로 로그인 성공")
        fun success() {
            val authResponse = AuthResponse("access-token", "refresh-token", true)
            whenever(authService.loginWithApple(any(), anyOrNull())).thenReturn(authResponse)

            mockMvc.perform(
                post("/api/auth/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("idToken" to "apple-token")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        }

        @Test
        @DisplayName("authorizationCode와 함께 로그인 성공")
        fun withAuthorizationCode_success() {
            val authResponse = AuthResponse("access-token", "refresh-token", true)
            whenever(authService.loginWithApple(any(), anyOrNull())).thenReturn(authResponse)

            mockMvc.perform(
                post("/api/auth/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("idToken" to "apple-token", "authorizationCode" to "auth-code-123")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
        }

        @Test
        @DisplayName("idToken이 빈 값이면 400 반환")
        fun blankToken_returns400() {
            mockMvc.perform(
                post("/api/auth/apple")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("idToken" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh")
    inner class RefreshToken {

        @Test
        @DisplayName("유효한 Refresh Token으로 토큰 재발급 성공")
        fun success() {
            val tokenResponse = TokenResponse("new-access", "new-refresh")
            whenever(authService.refreshToken(any())).thenReturn(tokenResponse)

            mockMvc.perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("refreshToken" to "valid-refresh-token")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh"))
        }

        @Test
        @DisplayName("refreshToken이 빈 값이면 400 반환")
        fun blankToken_returns400() {
            mockMvc.perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("refreshToken" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token이면 에러 반환")
        fun invalidToken_returnsError() {
            whenever(authService.refreshToken(any()))
                .thenThrow(BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH))

            mockMvc.perform(
                post("/api/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("refreshToken" to "invalid-token")))
            )
                .andExpect(status().isUnauthorized)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("A009"))
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    inner class Logout {

        @Test
        @DisplayName("인증된 유저 로그아웃 성공")
        fun success() {
            val accessToken = jwtTokenProvider.createAccessToken(1L)

            mockMvc.perform(
                post("/api/auth/logout")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("인증 토큰 없이 로그아웃 시도 시 에러 반환")
        fun withoutToken_returnsError() {
            mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isForbidden)
        }
    }
}
