package com.runnershi.domain.auth.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.auth.dto.AuthResponse
import com.runnershi.domain.auth.dto.GoogleLoginRequest
import com.runnershi.domain.auth.dto.KakaoLoginRequest
import com.runnershi.domain.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "카카오 로그인", description = "카카오 Access Token으로 로그인/회원가입")
    @PostMapping("/kakao")
    fun loginWithKakao(
        @Valid @RequestBody request: KakaoLoginRequest
    ): ApiResponse<AuthResponse> {
        val response = authService.loginWithKakao(request.accessToken)
        return ApiResponse.success(response)
    }

    @Operation(summary = "구글 로그인", description = "Google id_token으로 로그인/회원가입")
    @PostMapping("/google")
    fun loginWithGoogle(
        @Valid @RequestBody request: GoogleLoginRequest
    ): ApiResponse<AuthResponse> {
        val response = authService.loginWithGoogle(request.idToken)
        return ApiResponse.success(response)
    }
}
