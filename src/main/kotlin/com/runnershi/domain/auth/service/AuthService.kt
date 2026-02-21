package com.runnershi.domain.auth.service

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
    private val nicknameGenerator: NicknameGenerator
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val WITHDRAWAL_GRACE_DAYS = 30L
    }

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

        val (user, isNewUser) = when {
            existingUser == null -> {
                createNewUser(provider, userInfo) to true
            }
            existingUser.status == UserStatus.WITHDRAWN -> {
                handleWithdrawnUser(existingUser) to false
            }
            existingUser.status == UserStatus.DELETED -> {
                // DELETED 상태는 배치에서 providerId가 마스킹되므로 여기 도달하지 않지만 안전장치
                createNewUser(provider, userInfo) to true
            }
            else -> {
                updateLastLogin(existingUser)
                existingUser to false
            }
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

    private fun handleWithdrawnUser(user: User): User {
        val withdrawnAt = user.deletedAt
            ?: throw IllegalStateException("WITHDRAWN 상태인데 deletedAt이 null입니다: userId=${user.id}")

        val gracePeriodEnd = withdrawnAt.plusDays(WITHDRAWAL_GRACE_DAYS)

        return if (LocalDateTime.now().isBefore(gracePeriodEnd)) {
            // 유예 기간 내: 계정 복구
            log.info("탈퇴 유예 기간 내 복구: userId={}", user.id)
            user.status = UserStatus.ACTIVE
            user.deletedAt = null
            updateLastLogin(user)
            user
        } else {
            // 유예 기간 초과: 이 경우 배치에서 이미 처리했어야 하지만, 배치 전 접근 시 안전장치
            log.warn("유예 기간 초과 사용자 로그인 시도 (배치 미처리): userId={}", user.id)
            throw BusinessException(ErrorCode.USER_NOT_FOUND)
        }
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
