package com.runnershi.domain.mission.dto

import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import java.time.LocalDate
import java.time.LocalDateTime

// === 미션 그룹 응답 (미션 탭용) ===
// - 그룹 정보 + 소속 미션 목록 + 유저별 달성 상태 포함
data class MissionGroupResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val type: MissionGroupType,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val imageUrl: String?,
    val missions: List<MissionResponse>
)

// === 개별 미션 응답 ===
// - 미션 정보 + 유저 달성 상태/진행값 포함
data class MissionResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val imageUrl: String?,
    val conditionType: ConditionType,
    val conditionValue: Int?,
    val conditionStartDate: LocalDate?,
    val conditionEndDate: LocalDate?,
    val status: MissionStatus,
    val currentValue: Int
)

// === 홈 화면용 미션 이벤트 응답 ===
// - showOnHome=true인 활성 이벤트 중 최신 1개
data class HomeMissionResponse(
    val groupId: Long,
    val groupName: String,
    val description: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val missions: List<MissionResponse>
)

// === 내 미션 현황 응답 ===
data class MyMissionSummaryResponse(
    val totalMissions: Int,
    val achievedCount: Int,
    val inProgressCount: Int,
    val recentAchievements: List<AchievedMissionResponse>
)

data class AchievedMissionResponse(
    val missionId: Long,
    val missionName: String,
    val missionImageUrl: String?,
    val groupName: String,
    val conditionType: ConditionType,
    val achievedAt: LocalDateTime?
)
