package com.runnershi.domain.user.service

import com.runnershi.auth.client.AppleTokenClient
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.region.entity.District
import com.runnershi.domain.region.entity.Region
import com.runnershi.domain.region.entity.RegionType
import com.runnershi.domain.region.repository.DistrictRepository
import com.runnershi.domain.region.repository.RegionRepository
import com.runnershi.domain.terms.repository.TermsAgreementRepository
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
    lateinit var districtRepository: DistrictRepository

    @Mock
    lateinit var missionChecker: MissionChecker

    @Mock
    lateinit var termsAgreementRepository: TermsAgreementRepository

    @Mock
    lateinit var userMissionRepository: UserMissionRepository

    @Mock
    lateinit var userLevelSnapshotRepository: UserLevelSnapshotRepository

    @Mock
    lateinit var appleTokenClient: AppleTokenClient

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(
            userRepository,
            regionRepository,
            districtRepository,
            missionChecker,
            termsAgreementRepository,
            userMissionRepository,
            userLevelSnapshotRepository,
            appleTokenClient
        )
    }

    private fun createUser(
        id: Long = 1L,
        nickname: String = "테스트러너",
        regionId: Long? = null,
        status: UserStatus = UserStatus.ACTIVE,
        provider: Provider = Provider.KAKAO,
        providerId: String = "kakao-123",
        email: String? = "test@example.com"
    ): User {
        val user = User(
            provider = provider,
            providerId = providerId,
            email = email,
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

    private fun createDistrict(id: Long = 10L, regionId: Long = 1L, name: String = "서울특별시"): District {
        val district = District(regionId = regionId, name = name, isDefault = true)
        ReflectionTestUtils.setField(district, "id", id)
        return district
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
            user.districtId = 10L
            val region = createRegion()
            val district = createDistrict()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.findById(1L)).thenReturn(Optional.of(region))
            whenever(districtRepository.findById(10L)).thenReturn(Optional.of(district))

            val response = userService.getMyProfile(1L)

            assertEquals("테스트러너", response.nickname)
            assertNotNull(response.region)
            assertEquals("서울특별시", response.region!!.name)
            assertNotNull(response.district)
            assertEquals("서울특별시", response.district!!.name)
        }

        @Test
        @DisplayName("지역이 없는 유저 프로필 조회")
        fun withoutRegion() {
            val user = createUser(regionId = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            val response = userService.getMyProfile(1L)

            assertEquals("테스트러너", response.nickname)
            assertNull(response.region)
            assertNull(response.district)
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
            whenever(districtRepository.findFirstByRegionIdAndIsDefaultTrue(1L)).thenReturn(createDistrict())

            userService.updateRegion(1L, 1L, null)

            assertEquals(1L, user.regionId)
            assertEquals(10L, user.districtId)
            verify(missionChecker).checkOnRegionSet(1L)
        }

        @Test
        @DisplayName("districtId가 주어지면 해당 세부 지역으로 저장")
        fun successWithDistrictId() {
            val user = createUser()
            val district = createDistrict(id = 11L, name = "강남구")
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.existsById(1L)).thenReturn(true)
            whenever(districtRepository.findByIdAndRegionId(11L, 1L)).thenReturn(district)

            userService.updateRegion(1L, 1L, 11L)

            assertEquals(1L, user.regionId)
            assertEquals(11L, user.districtId)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.updateRegion(999L, 1L, null)
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
                userService.updateRegion(1L, 999L, null)
            }
            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("존재하지 않는 세부 지역 - 예외 발생")
        fun districtNotFound() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(regionRepository.existsById(1L)).thenReturn(true)
            whenever(districtRepository.findByIdAndRegionId(999L, 1L)).thenReturn(null)

            val exception = assertThrows<BusinessException> {
                userService.updateRegion(1L, 1L, 999L)
            }
            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("계정 탈퇴")
    inner class Withdraw {

        @Test
        @DisplayName("탈퇴 성공 - 개인정보 마스킹 및 연관 데이터 삭제")
        fun success() {
            val user = createUser()
            user.refreshToken = "some-token"
            user.refreshTokenExpiresAt = java.time.LocalDateTime.now().plusDays(7)
            user.profileImageUrl = "https://example.com/profile.png"
            user.fcmToken = "fcm-token"
            user.notificationEnabled = true
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            assertEquals(UserStatus.WITHDRAWN, user.status)
            assertNotNull(user.deletedAt)
            assertEquals("withdrawn_1", user.providerId)
            assertEquals("withdrawn_1", user.nickname)
            assertNull(user.email)
            assertNull(user.profileImageUrl)
            assertNull(user.regionId)
            assertNull(user.districtId)
            assertFalse(user.notificationEnabled)
            assertNull(user.fcmToken)
            verify(termsAgreementRepository).deleteAllByUserId(1L)
            verify(userMissionRepository).deleteAllByUserId(1L)
            verify(userLevelSnapshotRepository).deleteAllByUserId(1L)
            assertNull(user.refreshToken)
            assertNull(user.refreshTokenExpiresAt)
        }

        @Test
        @DisplayName("Apple 유저 탈퇴 시 revoke 후 refresh token 제거")
        fun appleUser_revokeToken() {
            val user = createUser(provider = Provider.APPLE, providerId = "apple-123", email = "apple@example.com")
            user.appleRefreshToken = "apple-refresh-token"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.withdraw(1L)

            verify(appleTokenClient).revokeRefreshToken("apple-refresh-token")
            assertNull(user.appleRefreshToken)
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
    }
}
