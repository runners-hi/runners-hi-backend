package com.runnershi.domain.auth.service

import com.runnershi.auth.client.AuthClient
import com.runnershi.auth.client.UserInfo
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.common.util.NicknameGenerator
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var jwtTokenProvider: JwtTokenProvider

    @Mock
    lateinit var nicknameGenerator: NicknameGenerator

    private lateinit var kakaoClient: AuthClient
    private lateinit var googleClient: AuthClient
    private lateinit var appleClient: AuthClient

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        kakaoClient = mock<AuthClient>()
        googleClient = mock<AuthClient>()
        appleClient = mock<AuthClient>()

        whenever(kakaoClient.getProvider()).thenReturn(Provider.KAKAO)
        whenever(googleClient.getProvider()).thenReturn(Provider.GOOGLE)
        whenever(appleClient.getProvider()).thenReturn(Provider.APPLE)

        authService = AuthService(
            userRepository,
            jwtTokenProvider,
            listOf(kakaoClient, googleClient, appleClient),
            nicknameGenerator
        )
    }

    private fun createUser(
        id: Long = 1L,
        provider: Provider = Provider.KAKAO,
        providerId: String = "provider-123",
        nickname: String = "테스트러너",
        status: UserStatus = UserStatus.ACTIVE
    ): User {
        val user = User(
            provider = provider,
            providerId = providerId,
            nickname = nickname,
            status = status
        )
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    private fun stubTokenCreation(userId: Long) {
        whenever(jwtTokenProvider.createAccessToken(userId)).thenReturn("access-token")
        whenever(jwtTokenProvider.createRefreshToken(userId)).thenReturn("refresh-token")
        whenever(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(1209600000L)
    }

    @Nested
    @DisplayName("소셜 로그인")
    inner class SocialLogin {

        @Test
        @DisplayName("카카오 로그인 - 신규 유저는 회원가입 후 토큰 반환")
        fun kakaoLogin_newUser() {
            val userInfo = UserInfo("kakao-123", "test@kakao.com", "카카오유저")
            val savedUser = createUser(provider = Provider.KAKAO, providerId = "kakao-123")

            whenever(kakaoClient.verify("kakao-token")).thenReturn(userInfo)
            whenever(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")).thenReturn(null)
            whenever(nicknameGenerator.generateUnique(any())).thenReturn("빠른치타1234")
            whenever(userRepository.save(any<User>())).thenReturn(savedUser)
            stubTokenCreation(savedUser.id)

            val response = authService.loginWithKakao("kakao-token")

            assertEquals("access-token", response.accessToken)
            assertEquals("refresh-token", response.refreshToken)
            assertTrue(response.isNewUser)
            verify(userRepository).save(any<User>())
        }

        @Test
        @DisplayName("카카오 로그인 - 기존 유저는 마지막 로그인 갱신 후 토큰 반환")
        fun kakaoLogin_existingUser() {
            val userInfo = UserInfo("kakao-123", "test@kakao.com", "카카오유저")
            val existingUser = createUser(provider = Provider.KAKAO, providerId = "kakao-123")

            whenever(kakaoClient.verify("kakao-token")).thenReturn(userInfo)
            whenever(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")).thenReturn(existingUser)
            stubTokenCreation(existingUser.id)

            val response = authService.loginWithKakao("kakao-token")

            assertEquals("access-token", response.accessToken)
            assertEquals("refresh-token", response.refreshToken)
            assertFalse(response.isNewUser)
            assertNotNull(existingUser.lastLoginAt)
        }

        @Test
        @DisplayName("구글 로그인 - 성공")
        fun googleLogin_success() {
            val userInfo = UserInfo("google-123", "test@gmail.com", "구글유저")
            val savedUser = createUser(provider = Provider.GOOGLE, providerId = "google-123")

            whenever(googleClient.verify("google-token")).thenReturn(userInfo)
            whenever(userRepository.findByProviderAndProviderId(Provider.GOOGLE, "google-123")).thenReturn(null)
            whenever(nicknameGenerator.generateUnique(any())).thenReturn("빠른치타1234")
            whenever(userRepository.save(any<User>())).thenReturn(savedUser)
            stubTokenCreation(savedUser.id)

            val response = authService.loginWithGoogle("google-token")

            assertTrue(response.isNewUser)
            assertEquals("access-token", response.accessToken)
        }

        @Test
        @DisplayName("애플 로그인 - 성공")
        fun appleLogin_success() {
            val userInfo = UserInfo("apple-123", "test@apple.com", null)
            val savedUser = createUser(provider = Provider.APPLE, providerId = "apple-123")

            whenever(appleClient.verify("apple-token")).thenReturn(userInfo)
            whenever(userRepository.findByProviderAndProviderId(Provider.APPLE, "apple-123")).thenReturn(null)
            whenever(nicknameGenerator.generateUnique(any())).thenReturn("빠른치타1234")
            whenever(userRepository.save(any<User>())).thenReturn(savedUser)
            stubTokenCreation(savedUser.id)

            val response = authService.loginWithApple("apple-token")

            assertTrue(response.isNewUser)
            assertEquals("access-token", response.accessToken)
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    inner class RefreshToken {

        @Test
        @DisplayName("유효한 Refresh Token으로 새 토큰 발급")
        fun refreshToken_success() {
            val user = createUser()
            user.refreshToken = "old-refresh-token"
            user.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7)

            whenever(jwtTokenProvider.getUserIdFromToken("old-refresh-token")).thenReturn(1L)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(jwtTokenProvider.createAccessToken(1L)).thenReturn("new-access-token")
            whenever(jwtTokenProvider.createRefreshToken(1L)).thenReturn("new-refresh-token")
            whenever(jwtTokenProvider.getRefreshTokenValidity()).thenReturn(1209600000L)

            val response = authService.refreshToken("old-refresh-token")

            assertEquals("new-access-token", response.accessToken)
            assertEquals("new-refresh-token", response.refreshToken)
            assertEquals("new-refresh-token", user.refreshToken)
        }

        @Test
        @DisplayName("Access Token으로 재발급 시도 시 예외 발생")
        fun refreshToken_withAccessToken_throwsException() {
            doThrow(BusinessException(ErrorCode.REFRESH_TOKEN_REQUIRED))
                .whenever(jwtTokenProvider).validateRefreshToken("access-token")

            val exception = assertThrows<BusinessException> {
                authService.refreshToken("access-token")
            }

            assertEquals(ErrorCode.REFRESH_TOKEN_REQUIRED, exception.errorCode)
        }

        @Test
        @DisplayName("존재하지 않는 유저의 Refresh Token으로 재발급 시도 시 예외 발생")
        fun refreshToken_userNotFound_throwsException() {
            whenever(jwtTokenProvider.getUserIdFromToken("refresh-token")).thenReturn(999L)
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                authService.refreshToken("refresh-token")
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("DB에 저장된 Refresh Token과 불일치 시 예외 발생")
        fun refreshToken_mismatch_throwsException() {
            val user = createUser()
            user.refreshToken = "stored-refresh-token"
            user.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7)

            whenever(jwtTokenProvider.getUserIdFromToken("different-token")).thenReturn(1L)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            val exception = assertThrows<BusinessException> {
                authService.refreshToken("different-token")
            }

            assertEquals(ErrorCode.REFRESH_TOKEN_MISMATCH, exception.errorCode)
        }

        @Test
        @DisplayName("만료된 Refresh Token으로 재발급 시도 시 예외 발생")
        fun refreshToken_expired_throwsException() {
            val user = createUser()
            user.refreshToken = "expired-token"
            user.refreshTokenExpiresAt = LocalDateTime.now().minusDays(1)

            whenever(jwtTokenProvider.getUserIdFromToken("expired-token")).thenReturn(1L)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            val exception = assertThrows<BusinessException> {
                authService.refreshToken("expired-token")
            }

            assertEquals(ErrorCode.EXPIRED_TOKEN, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("로그아웃")
    inner class Logout {

        @Test
        @DisplayName("로그아웃 시 Refresh Token 제거")
        fun logout_success() {
            val user = createUser()
            user.refreshToken = "some-token"
            user.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7)

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            authService.logout(1L)

            assertNull(user.refreshToken)
            assertNull(user.refreshTokenExpiresAt)
        }

        @Test
        @DisplayName("존재하지 않는 유저 로그아웃 시 예외 발생")
        fun logout_userNotFound_throwsException() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                authService.logout(999L)
            }

            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }
    }
}
