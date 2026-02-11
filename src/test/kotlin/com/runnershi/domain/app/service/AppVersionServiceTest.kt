package com.runnershi.domain.app.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.app.entity.AppVersion
import com.runnershi.domain.app.entity.Platform
import com.runnershi.domain.app.repository.AppVersionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppVersionServiceTest {

    @Mock lateinit var appVersionRepository: AppVersionRepository

    private lateinit var appVersionService: AppVersionService

    @BeforeEach
    fun setUp() {
        appVersionService = AppVersionService(appVersionRepository)
    }

    private fun stubAppVersion(platform: Platform, minVersion: String = "1.0.0", latestVersion: String = "2.0.0", updateUrl: String? = "https://store.example.com") {
        val appVersion = AppVersion(platform = platform, minVersion = minVersion, latestVersion = latestVersion, updateUrl = updateUrl)
        whenever(appVersionRepository.findByPlatform(platform)).thenReturn(appVersion)
    }

    @Nested
    @DisplayName("checkVersion")
    inner class CheckVersion {

        @Test
        @DisplayName("강제 업데이트 - 현재 버전 < minVersion")
        fun forceUpdate() {
            stubAppVersion(Platform.IOS, minVersion = "2.0.0", latestVersion = "3.0.0")

            val response = appVersionService.checkVersion(Platform.IOS, "1.5.0")

            assertTrue(response.forceUpdate)
            assertFalse(response.recommendUpdate)
            assertEquals("3.0.0", response.latestVersion)
        }

        @Test
        @DisplayName("권장 업데이트 - minVersion <= 현재 버전 < latestVersion")
        fun recommendUpdate() {
            stubAppVersion(Platform.IOS, minVersion = "1.0.0", latestVersion = "2.0.0")

            val response = appVersionService.checkVersion(Platform.IOS, "1.5.0")

            assertFalse(response.forceUpdate)
            assertTrue(response.recommendUpdate)
            assertEquals("2.0.0", response.latestVersion)
        }

        @Test
        @DisplayName("최신 버전 - 현재 버전 >= latestVersion")
        fun upToDate() {
            stubAppVersion(Platform.IOS, minVersion = "1.0.0", latestVersion = "2.0.0")

            val response = appVersionService.checkVersion(Platform.IOS, "2.0.0")

            assertFalse(response.forceUpdate)
            assertFalse(response.recommendUpdate)
        }

        @Test
        @DisplayName("최신 버전보다 높은 버전")
        fun higherThanLatest() {
            stubAppVersion(Platform.ANDROID, minVersion = "1.0.0", latestVersion = "2.0.0")

            val response = appVersionService.checkVersion(Platform.ANDROID, "3.0.0")

            assertFalse(response.forceUpdate)
            assertFalse(response.recommendUpdate)
        }

        @Test
        @DisplayName("정확히 minVersion과 같은 경우 - 강제 업데이트 아님")
        fun exactMinVersion() {
            stubAppVersion(Platform.IOS, minVersion = "1.0.0", latestVersion = "2.0.0")

            val response = appVersionService.checkVersion(Platform.IOS, "1.0.0")

            assertFalse(response.forceUpdate)
            assertTrue(response.recommendUpdate)
        }

        @Test
        @DisplayName("시맨틱 버전 비교 - 마이너/패치 비교")
        fun semanticVersionComparison() {
            stubAppVersion(Platform.IOS, minVersion = "1.2.0", latestVersion = "1.3.0")

            val response = appVersionService.checkVersion(Platform.IOS, "1.2.5")

            assertFalse(response.forceUpdate)
            assertTrue(response.recommendUpdate)
        }

        @Test
        @DisplayName("updateUrl 포함 확인")
        fun includesUpdateUrl() {
            stubAppVersion(Platform.ANDROID, updateUrl = "https://play.google.com/store/apps")

            val response = appVersionService.checkVersion(Platform.ANDROID, "0.1.0")

            assertEquals("https://play.google.com/store/apps", response.updateUrl)
        }

        @Test
        @DisplayName("지원하지 않는 플랫폼 - 예외 발생")
        fun platformNotFound() {
            whenever(appVersionRepository.findByPlatform(Platform.IOS)).thenReturn(null)

            val exception = assertThrows<BusinessException> {
                appVersionService.checkVersion(Platform.IOS, "1.0.0")
            }
            assertEquals(ErrorCode.PLATFORM_NOT_FOUND, exception.errorCode)
        }
    }
}
