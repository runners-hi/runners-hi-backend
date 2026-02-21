package com.runnershi.domain.auth.service

import com.runnershi.auth.client.AppleClient
import com.runnershi.auth.client.AuthClient
import com.runnershi.auth.client.UserInfo
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.common.util.NicknameGenerator
import com.runnershi.domain.auth.dto.AuthResponse
import com.runnershi.domain.auth.dto.TokenResponse
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val authClients: List<AuthClient>,
    private val nicknameGenerator: NicknameGenerator,
    private val appleClient: AppleClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

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
    fun loginWithApple(idToken: String, authorizationCode: String? = null): AuthResponse {
        val client = getClient(Provider.APPLE)
        val userInfo = client.verify(idToken)
        val response = processLogin(Provider.APPLE, userInfo)

        if (authorizationCode != null) {
            try {
                val appleRefreshToken = appleClient.exchangeCodeForRefreshToken(authorizationCode)
                if (appleRefreshToken != null) {
                    val user = userRepository.findByProviderAndProviderId(Provider.APPLE, userInfo.providerId)
                    user?.appleRefreshToken = appleRefreshToken
                }
            } catch (e: Exception) {
                log.warn("Apple refresh token 교환 실패, 로그인은 정상 진행: {}", e.message)
            }
        }

        return response
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
        // TODO: 기본 프로필 이미지 URL 목록 (4~5개) 확정 후 랜덤 부여 로직 추가
        //       val profileImageUrl = DefaultProfileImages.random()

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

    @Transactional
    fun refreshToken(refreshToken: String): TokenResponse {
        // 1. Refresh Token 타입 검증
        jwtTokenProvider.validateRefreshToken(refreshToken)

        // 2. 토큰에서 userId 추출
        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)

        // 3. 사용자 조회
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 4. DB에 저장된 Refresh Token과 일치하는지 확인
        if (user.refreshToken != refreshToken) {
            throw BusinessException(ErrorCode.REFRESH_TOKEN_MISMATCH)
        }

        // 5. Refresh Token 만료 여부 확인
        if (user.refreshTokenExpiresAt?.isBefore(LocalDateTime.now()) == true) {
            throw BusinessException(ErrorCode.EXPIRED_TOKEN)
        }

        // 6. 새 토큰 발급 (Refresh Token Rotation)
        val newAccessToken = jwtTokenProvider.createAccessToken(user.id)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.id)

        // 7. 새 Refresh Token 저장
        user.refreshToken = newRefreshToken
        user.refreshTokenExpiresAt = LocalDateTime.now()
            .plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000)

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    @Transactional
    fun logout(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.refreshToken = null
        user.refreshTokenExpiresAt = null
    }
}
