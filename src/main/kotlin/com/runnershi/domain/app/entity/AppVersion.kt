package com.runnershi.domain.app.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

// === 앱 버전 관리 ===
// - 플랫폼별(iOS/Android) 최소 버전, 최신 버전 관리
// - minVersion 미만 → 강제 업데이트
// - latestVersion 미만 → 권장 업데이트
// - DB에서 직접 관리 (재배포 없이 변경 가능)
@Entity
@Table(
    name = "app_versions",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["platform"])
    ]
)
class AppVersion(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val platform: Platform,

    // 이 버전 미만이면 강제 업데이트
    @Column(name = "min_version", nullable = false, length = 20)
    var minVersion: String,

    // 최신 버전
    @Column(name = "latest_version", nullable = false, length = 20)
    var latestVersion: String,

    // 스토어 URL
    @Column(name = "update_url", length = 500)
    var updateUrl: String? = null
) : BaseEntity()

enum class Platform {
    IOS,
    ANDROID
}
