package com.runnershi.auth.client

import com.runnershi.domain.user.entity.Provider

interface AuthClient {
    fun getProvider(): Provider
    fun verify(token: String): UserInfo
}

data class UserInfo(
    val providerId: String,
    val email: String?,
    val nickname: String?
)
