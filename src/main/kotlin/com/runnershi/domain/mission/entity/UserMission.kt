package com.runnershi.domain.mission.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

// === 유저별 미션 달성 상태 ===
// - 유저가 미션에 참여하면 레코드 생성 (NOT_ACHIEVED)
// - 진행형 미션(CUMULATIVE_DISTANCE 등)은 currentValue로 진행률 추적
// - 달성 시 status → ACHIEVED, achievedAt 기록
@Entity
@Table(
    name = "user_missions",
    indexes = [
        Index(name = "idx_user_missions_user_id", columnList = "user_id")
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "mission_id"])
    ]
)
class UserMission(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "mission_id", nullable = false)
    val missionId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: MissionStatus = MissionStatus.NOT_ACHIEVED,

    // 진행 값 (누적 거리 등 진행형 미션에서 사용)
    @Column(name = "current_value", nullable = false)
    var currentValue: Int = 0,

    @Column(name = "achieved_at")
    var achievedAt: LocalDateTime? = null
) : BaseEntity()
