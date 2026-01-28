package com.runnershi.domain.auth.service

import com.runnershi.auth.client.AuthClient
import com.runnershi.auth.client.UserInfo
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.common.util.NicknameGenerator
import com.runnershi.domain.auth.dto.AuthResponse
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authClients: List<AuthClient>,
    private val nicknameGenerator: NicknameGenerator
) {

    @Transactional
    fun loginWithKakao(accessToken: String): AuthResponse {
        val client = getClient(Provider.KAKAO)
        val userInfo = client.verify(accessToken)
        return processLogin(Provider.KAKAO, userInfo)
    }

    @Transactional
    fun loginWithGoogle(idToken: String): AuthResponse {
        val client = getClient(Provider.GOOGLE)
        val userInfo = client.verify(idToken)
        return processLogin(Provider.GOOGLE, userInfo)
    }

    @Transactional
    fun loginWithApple(idToken: String): AuthResponse {
        val client = getClient(Provider.APPLE)
        val userInfo = client.verify(idToken)
        return processLogin(Provider.APPLE, userInfo)
    }

    private fun getClient(provider: Provider): AuthClient {
        return authClients.find { it.getProvider() == provider }
            ?: throw IllegalStateException("AuthClient not found: $provider")
    }

    private fun processLogin(provider: Provider, userInfo: UserInfo): AuthResponse {
        val existingUser = userRepository.findByProviderAndProviderId(provider, userInfo.providerId)

        val (user, isNewUser) = if (existingUser != null) {
            updateLastLogin(existingUser)
            existingUser to false
        } else {
            createNewUser(provider, userInfo) to true
        }

        val accessToken = jwtTokenProvider.createAccessToken(user.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)

        // Refresh Token 저장
        user.refreshToken = refreshToken
        user.refreshTokenExpiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            isNewUser = isNewUser
        )
    }

    private fun createNewUser(provider: Provider, userInfo: UserInfo): User {
        val nickname = generateUniqueNickname()

        val user = User(
            provider = provider,
            providerId = userInfo.providerId,
            email = userInfo.email,
            nickname = nickname,
            status = UserStatus.PENDING
        )

        return userRepository.save(user)
    }

    private fun updateLastLogin(user: User) {
        user.lastLoginAt = LocalDateTime.now()
    }

    private fun generateUniqueNickname(): String {
        return nicknameGenerator.generateUnique { nickname ->
            userRepository.existsByNickname(nickname)
        }
    }
}
