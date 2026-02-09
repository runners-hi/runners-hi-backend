package com.runnershi.domain.app.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.app.dto.AppVersionCheckResponse
import com.runnershi.domain.app.entity.Platform
import com.runnershi.domain.app.repository.AppVersionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AppVersionService(
    private val appVersionRepository: AppVersionRepository
) {

    @Transactional(readOnly = true)
    fun checkVersion(platform: Platform, currentVersion: String): AppVersionCheckResponse {
        val appVersion = appVersionRepository.findByPlatform(platform)
            ?: throw BusinessException(ErrorCode.PLATFORM_NOT_FOUND)

        val forceUpdate = compareVersions(currentVersion, appVersion.minVersion) < 0
        val recommendUpdate = !forceUpdate && compareVersions(currentVersion, appVersion.latestVersion) < 0

        return AppVersionCheckResponse(
            forceUpdate = forceUpdate,
            recommendUpdate = recommendUpdate,
            latestVersion = appVersion.latestVersion,
            updateUrl = appVersion.updateUrl
        )
    }

    // 시맨틱 버전 비교: "1.2.3" vs "1.3.0" → -1
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }
}
