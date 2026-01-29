package com.runnershi.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class KakaoLoginRequest(
    @field:NotBlank(message = "accessToken은 필수입니다")
    val accessToken: String
)

data class GoogleLoginRequest(
    @field:NotBlank(message = "idToken은 필수입니다")
    val idToken: String
)

data class AppleLoginRequest(
    @field:NotBlank(message = "idToken은 필수입니다")
    val idToken: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "refreshToken은 필수입니다")
    val refreshToken: String
)
