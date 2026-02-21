package com.runnershi.domain.user.service

import com.runnershi.auth.client.GoogleClient
import com.runnershi.auth.client.KakaoClient
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.region.entity.Region
import com.runnershi.domain.region.entity.RegionType
import com.runnershi.domain.region.repository.RegionRepository
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
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var regionRepository: RegionRepository

    @Mock
    lateinit var missionChecker: MissionChecker

    @Mock
    lateinit var googleClient: GoogleClient

    @Mock
    lateinit var kakaoClient: KakaoClient

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, regionRepository, missionChecker, googleClient, kakaoClient)
    }

    private fun createUser(
        id: Long = 1L,
        provider: Provider = Provider.KAKAO,
        providerId: String = "kakao-123",
        nickname: String = "테스트러너",
        regionId: Long? = null,
        status: UserStatus = UserStatus.ACTIVE
    ): User {
        val user = User(
            provider = provider,
            providerId = providerId,
            nickname = nickname,
            status = status
        )
        user.regionId = regionId
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    private fun createRegion(id: Long = 1L, name: String = "서울특별시"): Region {
        val region = Region(name = name, type = RegionType.SPECIAL_CITY)
        ReflectionTestUtils.setField(region, "id", id)
        return region
    }

    @Nested
    @DisplayName("닉네임 변경")
    inner class UpdateNickname {

        @Test
        @DisplayName("닉네임 변경 성공")
        fun success() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userRepository.existsByNickname("새닉네임")).thenReturn(false)

            userService.updateNickname(1L, "새닉네임")

            assertEquals("새닉네임", user.nickname)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.updateNickname(999L, "새닉네임")
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("중복된 닉네임 - 예외 발생")
        fun duplicateNickname() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userRepository.existsByNickname("중복닉네임")).thenReturn(true)

            val exception = assertThrows<BusinessException> {
                userService.updateNickname(1L, "중복닉네임")
            }
            assertEquals(ErrorCode.DUPLICATE_NICKNAME, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("닉네임 중복 체크")
    inner class CheckNicknameAvailable {

        @Test
        @DisplayName("사용 가능한 닉네임")
        fun available() {
            whenever(userRepository.existsByNickname("새닉네임")).thenReturn(false)

            assertTrue(userService.checkNicknameAvailable("새닉네임"))
        }

        @Test
        @DisplayName("이미 사용 중인 닉네임")
        fun notAvailable() {
            whenever(userRepository.existsByNickname("중복닉네임")).thenReturn(true)

            assertFalse(userService.checkNicknameAvailable("중복닉네임"))
        }
    }

    @Nested
    @DisplayName("내 프로필 조회")
    inner class GetMyProfile {

        @Test
        @DisplayName("지역이 있는 유저 프로필 조회")
        fun withRegion() {
            val user = createUser(regionId = 1L)
            val region = createRegion()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.findById(1L)).thenReturn(Optional.of(region))

            val response = userService.getMyProfile(1L)

            assertEquals("테스트러너", response.nickname)
            assertNotNull(response.region)
            assertEquals("서울특별시", response.region!!.name)
        }

        @Test
        @DisplayName("지역이 없는 유저 프로필 조회")
        fun withoutRegion() {
            val user = createUser(regionId = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            val response = userService.getMyProfile(1L)

            assertEquals("테스트러너", response.nickname)
            assertNull(response.region)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.getMyProfile(999L)
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("지역 변경")
    inner class UpdateRegion {

        @Test
        @DisplayName("지역 변경 성공 및 미션 체크 호출")
        fun success() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.existsById(1L)).thenReturn(true)

            userService.updateRegion(1L, 1L)

            assertEquals(1L, user.regionId)
            verify(missionChecker).checkOnRegionSet(1L)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.updateRegion(999L, 1L)
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("존재하지 않는 지역 - 예외 발생")
        fun regionNotFound() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.existsById(999L)).thenReturn(false)

            val exception = assertThrows<BusinessException> {
                userService.updateRegion(1L, 999L)
            }
            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("계정 탈퇴")
    inner class Withdraw {

        @Test
        @DisplayName("탈퇴 성공 - 상태 변경 및 토큰 제거")
        fun success() {
            val user = createUser()
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            assertEquals(UserStatus.WITHDRAWN, user.status)
            assertNotNull(user.deletedAt)
            assertNull(user.refreshToken)
            assertNull(user.refreshTokenExpiresAt)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.withdraw(999L)
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("Kakao 유저 탈퇴 시 unlinkUser 호출")
        fun kakaoUserWithdraw_callsUnlink() {
            val user = createUser(provider = Provider.KAKAO, providerId = "kakao-456")
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            verify(kakaoClient).unlinkUser("kakao-456")
            verify(googleClient, never()).revokeToken(any())
            assertEquals(UserStatus.WITHDRAWN, user.status)
        }

        @Test
        @DisplayName("Google 유저 탈퇴 시 revokeToken 호출 (로그만 남김)")
        fun googleUserWithdraw_callsRevoke() {
            val user = createUser(provider = Provider.GOOGLE, providerId = "google-789")
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            verify(googleClient).revokeToken("google-789")
            verify(kakaoClient, never()).unlinkUser(any())
            assertEquals(UserStatus.WITHDRAWN, user.status)
        }

        @Test
        @DisplayName("Apple 유저 탈퇴 시 서버 revoke 없이 탈퇴 진행")
        fun appleUserWithdraw_noServerRevoke() {
            val user = createUser(provider = Provider.APPLE, providerId = "apple-999")
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            verify(kakaoClient, never()).unlinkUser(any())
            verify(googleClient, never()).revokeToken(any())
            assertEquals(UserStatus.WITHDRAWN, user.status)
            assertNotNull(user.deletedAt)
        }

        @Test
        @DisplayName("Kakao revoke 실패해도 탈퇴는 정상 진행")
        fun kakaoRevokeFails_withdrawContinues() {
            val user = createUser(provider = Provider.KAKAO, providerId = "kakao-456")
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            doThrow(RuntimeException("Kakao API 오류"))
                .whenever(kakaoClient).unlinkUser("kakao-456")

            userService.withdraw(1L)

            assertEquals(UserStatus.WITHDRAWN, user.status)
            assertNotNull(user.deletedAt)
            assertNull(user.refreshToken)
        }

        @Test
        @DisplayName("Google revoke 실패해도 탈퇴는 정상 진행")
        fun googleRevokeFails_withdrawContinues() {
            val user = createUser(provider = Provider.GOOGLE, providerId = "google-789")
            user.refreshToken = "some-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            doThrow(RuntimeException("Google API 오류"))
                .whenever(googleClient).revokeToken("google-789")

            userService.withdraw(1L)

            assertEquals(UserStatus.WITHDRAWN, user.status)
            assertNotNull(user.deletedAt)
            assertNull(user.refreshToken)
        }
    }
}
