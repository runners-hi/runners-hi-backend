package com.runnershi.domain.app.dto

// === 앱 버전 체크 응답 ===
// - forceUpdate: true → 강제 업데이트 (현재 버전 < minVersion)
// - recommendUpdate: true → 권장 업데이트 (현재 버전 < latestVersion)
// - 둘 다 false → 최신 버전
data class AppVersionCheckResponse(
    val forceUpdate: Boolean,
    val recommendUpdate: Boolean,
    val latestVersion: String,
    val updateUrl: String?
)
