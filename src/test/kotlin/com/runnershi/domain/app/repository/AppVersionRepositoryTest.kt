package com.runnershi.domain.app.repository

import com.runnershi.domain.app.entity.AppVersion
import com.runnershi.domain.app.entity.Platform
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AppVersionRepositoryTest : RepositoryTest() {

    @Autowired lateinit var appVersionRepository: AppVersionRepository

    @BeforeEach
    fun setUp() {
        appVersionRepository.saveAll(listOf(
            AppVersion(platform = Platform.IOS, minVersion = "1.0.0", latestVersion = "2.0.0", updateUrl = "https://apps.apple.com"),
            AppVersion(platform = Platform.ANDROID, minVersion = "1.0.0", latestVersion = "2.1.0", updateUrl = "https://play.google.com")
        ))
    }

    @Test
    @DisplayName("플랫폼별 앱 버전 조회 - IOS")
    fun findByPlatform_ios() {
        val result = appVersionRepository.findByPlatform(Platform.IOS)

        assertNotNull(result)
        assertEquals(Platform.IOS, result.platform)
        assertEquals("1.0.0", result.minVersion)
        assertEquals("2.0.0", result.latestVersion)
        assertEquals("https://apps.apple.com", result.updateUrl)
    }

    @Test
    @DisplayName("플랫폼별 앱 버전 조회 - ANDROID")
    fun findByPlatform_android() {
        val result = appVersionRepository.findByPlatform(Platform.ANDROID)

        assertNotNull(result)
        assertEquals(Platform.ANDROID, result.platform)
        assertEquals("2.1.0", result.latestVersion)
    }

    @Test
    @DisplayName("등록되지 않은 플랫폼 조회 시 null")
    fun findByPlatform_notFound() {
        appVersionRepository.deleteAll()

        val result = appVersionRepository.findByPlatform(Platform.IOS)

        assertNull(result)
    }
}
