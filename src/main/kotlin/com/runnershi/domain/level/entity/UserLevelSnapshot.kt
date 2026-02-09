package com.runnershi.domain.level.entity

import com.runnershi.domain.user.entity.Tier
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// === 유저 레벨/티어 스냅샷 ===
// - 연도별/월별 레벨, 티어, 경험치, 총 거리 기록
// - 연초 초기화 시 직전년도 최종 티어 확인용
// - 통계/분석 데이터로 활용 가능
// TODO: 스냅샷 저장 시점/방식은 연초 초기화 정책 확정 후 구현
//       (월별 스케줄러 or 연말 일괄 생성 등)
@Entity
@Table(
    name = "user_level_snapshot",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "year", "month"])
    ]
)
class UserLevelSnapshot(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val year: Int,

    @Column(nullable = false)
    val month: Int,

    @Column(nullable = false)
    val level: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tier: Tier,

    @Column(nullable = false)
    val experience: Int,

    @Column(name = "total_distance", nullable = false)
    val totalDistance: Int,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
