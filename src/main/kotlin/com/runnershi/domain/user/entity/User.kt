package com.runnershi.domain.user.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["provider", "provider_id"]),
        UniqueConstraint(columnNames = ["nickname"])
    ],
    indexes = [
        Index(name = "idx_users_region_distance", columnList = "region_id, total_distance")
    ]
)
class User(
    // 소셜 로그인
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val provider: Provider,

    @Column(name = "provider_id", nullable = false)
    val providerId: String,

    val email: String? = null,

    // 프로필
    @Column(nullable = false)
    var nickname: String,

    var profileImageUrl: String? = null,

    // 지역
    @Column(name = "region_id")
    var regionId: Long? = null,

    // 등급/레벨
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var tier: Tier = Tier.BRONZE,

    @Column(nullable = false)
    var level: Int = 1,

    @Column(nullable = false)
    var experience: Int = 0,

    // 단위: m (미터)
    @Column(name = "total_distance", nullable = false)
    var totalDistance: Int = 0,

    // 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: UserStatus = UserStatus.PENDING,

    // 알림
    @Column(nullable = false)
    var notificationEnabled: Boolean = true,

    var fcmToken: String? = null,

    // 토큰 (stateful 방식)
    var refreshToken: String? = null,
    var refreshTokenExpiresAt: LocalDateTime? = null,

    // Apple refresh token (계정 탈퇴 시 revoke용)
    var appleRefreshToken: String? = null,

    // 시간
    var lastLoginAt: LocalDateTime? = null,
    var deletedAt: LocalDateTime? = null
) : BaseEntity()
