package com.runnershi.domain.mission.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate

// === 개별 미션 ===
// - missionGroupId로 그룹에 소속
// - conditionType + conditionValue + conditionStartDate/EndDate로 달성 조건 정의
// - 조건 예시:
//   SINGLE_DISTANCE(conditionValue=5000)       → 1회 러닝 5km 달성
//   CUMULATIVE_DISTANCE(conditionValue=35000)   → 누적 35km 달성
//   PACE(conditionValue=300)                    → 페이스 5'00" 이하 달성
//   RUN_ON_DATE(conditionStartDate=10/31)       → 10월 31일에 러닝
//   RUN_IN_PERIOD(conditionStartDate~EndDate)   → 특정 기간 내 러닝
//   FIRST_RUN                                   → 첫 러닝 등록
//   REGION_SET                                  → 지역 인증
@Entity
@Table(
    name = "missions",
    indexes = [
        Index(name = "idx_missions_group_id", columnList = "mission_group_id")
    ]
)
class Mission(
    @Column(name = "mission_group_id", nullable = false)
    val missionGroupId: Long,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_type", nullable = false, length = 30)
    val conditionType: ConditionType,

    // 거리(m), 페이스(초/km) 등 수치 조건
    @Column(name = "condition_value")
    val conditionValue: Int? = null,

    // 날짜/기간 조건용
    @Column(name = "condition_start_date")
    val conditionStartDate: LocalDate? = null,

    @Column(name = "condition_end_date")
    val conditionEndDate: LocalDate? = null
) : BaseEntity()
