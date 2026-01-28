package com.runnershi.domain.auth.dto

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean
)
