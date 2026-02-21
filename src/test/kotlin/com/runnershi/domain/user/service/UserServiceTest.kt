package com.runnershi.domain.user.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.storage.PresignedUrlInfo
import com.runnershi.common.storage.StorageService
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
    lateinit var storageService: StorageService

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository, regionRepository, missionChecker, storageService)
    }

    private fun createUser(
        id: Long = 1L,
        nickname: String = "테스트러너",
        regionId: Long? = null,
        status: UserStatus = UserStatus.ACTIVE
    ): User {
        val user = User(
            provider = Provider.KAKAO,
            providerId = "kakao-123",
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
    }

    @Nested
    @DisplayName("프로필 이미지 업로드 URL 발급")
    inner class GenerateProfileImageUploadUrl {

        @Test
        @DisplayName("JPEG 이미지 업로드 URL 발급 성공")
        fun successWithJpeg() {
            val presignedUrlInfo = PresignedUrlInfo(
                url = "https://storage.googleapis.com/test-bucket/presigned-url",
                objectKey = "profiles/1/test-uuid.jpg"
            )
            whenever(storageService.generatePresignedUploadUrl(1L, "image/jpeg")).thenReturn(presignedUrlInfo)

            val response = userService.generateProfileImageUploadUrl(1L, "image/jpeg")

            assertEquals("https://storage.googleapis.com/test-bucket/presigned-url", response.presignedUrl)
            assertEquals("profiles/1/test-uuid.jpg", response.objectKey)
        }

        @Test
        @DisplayName("PNG 이미지 업로드 URL 발급 성공")
        fun successWithPng() {
            val presignedUrlInfo = PresignedUrlInfo(
                url = "https://storage.googleapis.com/test-bucket/presigned-url",
                objectKey = "profiles/1/test-uuid.png"
            )
            whenever(storageService.generatePresignedUploadUrl(1L, "image/png")).thenReturn(presignedUrlInfo)

            val response = userService.generateProfileImageUploadUrl(1L, "image/png")

            assertEquals("profiles/1/test-uuid.png", response.objectKey)
        }

        @Test
        @DisplayName("WebP 이미지 업로드 URL 발급 성공")
        fun successWithWebp() {
            val presignedUrlInfo = PresignedUrlInfo(
                url = "https://storage.googleapis.com/test-bucket/presigned-url",
                objectKey = "profiles/1/test-uuid.webp"
            )
            whenever(storageService.generatePresignedUploadUrl(1L, "image/webp")).thenReturn(presignedUrlInfo)

            val response = userService.generateProfileImageUploadUrl(1L, "image/webp")

            assertEquals("profiles/1/test-uuid.webp", response.objectKey)
        }

        @Test
        @DisplayName("허용되지 않은 contentType - 예외 발생")
        fun unsupportedContentType() {
            val exception = assertThrows<BusinessException> {
                userService.generateProfileImageUploadUrl(1L, "image/gif")
            }
            assertEquals(ErrorCode.INVALID_IMAGE_TYPE, exception.errorCode)
        }

        @Test
        @DisplayName("objectKey 형식이 profiles/{userId}/ 으로 시작")
        fun objectKeyFormat() {
            val presignedUrlInfo = PresignedUrlInfo(
                url = "https://storage.googleapis.com/test-bucket/presigned-url",
                objectKey = "profiles/42/some-uuid.jpg"
            )
            whenever(storageService.generatePresignedUploadUrl(42L, "image/jpeg")).thenReturn(presignedUrlInfo)

            val response = userService.generateProfileImageUploadUrl(42L, "image/jpeg")

            assertTrue(response.objectKey.startsWith("profiles/42/"))
            assertTrue(response.objectKey.endsWith(".jpg"))
        }
    }

    @Nested
    @DisplayName("프로필 이미지 확정")
    inner class ConfirmProfileImage {

        @Test
        @DisplayName("이미지 확정 성공 - profileImageUrl 업데이트")
        fun success() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.confirmProfileImage(1L, "profiles/1/new-image.jpg")

            assertEquals("profiles/1/new-image.jpg", user.profileImageUrl)
        }

        @Test
        @DisplayName("이전 이미지가 있으면 삭제 후 새 이미지 저장")
        fun deletePreviousImage() {
            val user = createUser()
            user.profileImageUrl = "profiles/1/old-image.jpg"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.confirmProfileImage(1L, "profiles/1/new-image.jpg")

            verify(storageService).deleteObject("profiles/1/old-image.jpg")
            assertEquals("profiles/1/new-image.jpg", user.profileImageUrl)
        }

        @Test
        @DisplayName("이전 이미지가 없으면 삭제 호출 안 함")
        fun noPreviousImage() {
            val user = createUser()
            assertNull(user.profileImageUrl)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.confirmProfileImage(1L, "profiles/1/new-image.jpg")

            verify(storageService, never()).deleteObject(any())
            assertEquals("profiles/1/new-image.jpg", user.profileImageUrl)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.confirmProfileImage(999L, "profiles/999/image.jpg")
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("다른 유저의 objectKey로 확정 시도 - 예외 발생")
        fun invalidObjectKey_otherUser() {
            val exception = assertThrows<BusinessException> {
                userService.confirmProfileImage(1L, "profiles/999/malicious.jpg")
            }
            assertEquals(ErrorCode.INVALID_OBJECT_KEY, exception.errorCode)
        }

        @Test
        @DisplayName("잘못된 형식의 objectKey - 예외 발생")
        fun invalidObjectKey_wrongFormat() {
            val exception = assertThrows<BusinessException> {
                userService.confirmProfileImage(1L, "some/random/path.jpg")
            }
            assertEquals(ErrorCode.INVALID_OBJECT_KEY, exception.errorCode)
        }

        @Test
        @DisplayName("이전 이미지 삭제 실패해도 새 이미지 확정은 진행")
        fun deleteFailure_stillConfirms() {
            val user = createUser()
            user.profileImageUrl = "profiles/1/old-image.jpg"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            doThrow(RuntimeException("GCS 오류"))
                .whenever(storageService).deleteObject("profiles/1/old-image.jpg")

            userService.confirmProfileImage(1L, "profiles/1/new-image.jpg")

            assertEquals("profiles/1/new-image.jpg", user.profileImageUrl)
        }
    }

    @Nested
    @DisplayName("프로필 이미지 삭제")
    inner class RemoveProfileImage {

        @Test
        @DisplayName("이미지 삭제 성공 - profileImageUrl null 처리")
        fun success() {
            val user = createUser()
            user.profileImageUrl = "profiles/1/old-image.jpg"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.removeProfileImage(1L)

            verify(storageService).deleteObject("profiles/1/old-image.jpg")
            assertNull(user.profileImageUrl)
        }

        @Test
        @DisplayName("이미지가 없는 유저 삭제 시 GCS 삭제 호출 안 함")
        fun noImage() {
            val user = createUser()
            assertNull(user.profileImageUrl)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            userService.removeProfileImage(1L)

            verify(storageService, never()).deleteObject(any())
            assertNull(user.profileImageUrl)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                userService.removeProfileImage(999L)
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }

        @Test
        @DisplayName("GCS 삭제 실패해도 profileImageUrl은 null 처리")
        fun deleteFailure_stillRemoves() {
            val user = createUser()
            user.profileImageUrl = "profiles/1/old-image.jpg"
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            doThrow(RuntimeException("GCS 오류"))
                .whenever(storageService).deleteObject("profiles/1/old-image.jpg")

            userService.removeProfileImage(1L)

            assertNull(user.profileImageUrl)
        }
    }
}
