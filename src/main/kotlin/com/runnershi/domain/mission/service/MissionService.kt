package com.runnershi.domain.mission.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.mission.dto.AchievedMissionResponse
import com.runnershi.domain.mission.dto.HomeMissionResponse
import com.runnershi.domain.mission.dto.MissionGroupResponse
import com.runnershi.domain.mission.dto.MissionResponse
import com.runnershi.domain.mission.dto.MyMissionSummaryResponse
import com.runnershi.domain.mission.entity.Mission
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import com.runnershi.domain.mission.repository.MissionGroupRepository
import com.runnershi.domain.mission.repository.MissionRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class MissionService(
    private val missionGroupRepository: MissionGroupRepository,
    private val missionRepository: MissionRepository,
    private val userMissionRepository: UserMissionRepository
) {

    // 미션 탭: 전체 미션 그룹 + 미션 목록 + 유저 달성 상태
    // - N+1 방지: 그룹 ID 목록으로 미션 일괄 조회, 미션 ID 목록으로 유저미션 일괄 조회
    @Transactional(readOnly = true)
    fun getMissionGroups(userId: Long): List<MissionGroupResponse> {
        val groups = missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()
        if (groups.isEmpty()) return emptyList()

        val groupIds = groups.map { it.id }
        val missions = missionRepository.findByMissionGroupIdIn(groupIds)
        val userMissionMap = getUserMissionMap(userId, missions)
        val missionsByGroup = missions.groupBy { it.missionGroupId }

        return groups.map { group ->
            MissionGroupResponse(
                id = group.id,
                name = group.name,
                description = group.description,
                type = group.type,
                startDate = group.startDate,
                endDate = group.endDate,
                imageUrl = group.imageUrl,
                missions = (missionsByGroup[group.id] ?: emptyList()).map { mission ->
                    toMissionResponse(mission, userMissionMap[mission.id])
                }
            )
        }
    }

    // 홈 화면: showOnHome=true인 활성 이벤트 중 최신 1개
    @Transactional(readOnly = true)
    fun getHomeMissions(userId: Long): HomeMissionResponse? {
        val group = missionGroupRepository.findActiveForHome(LocalDate.now())
            .firstOrNull() ?: return null

        val missions = missionRepository.findByMissionGroupId(group.id)
        val userMissionMap = getUserMissionMap(userId, missions)

        return HomeMissionResponse(
            groupId = group.id,
            groupName = group.name,
            description = group.description,
            startDate = group.startDate,
            endDate = group.endDate,
            missions = missions.map { mission ->
                toMissionResponse(mission, userMissionMap[mission.id])
            }
        )
    }

    // 미션 그룹 상세: 특정 그룹의 미션 목록 + 유저 달성 상태
    @Transactional(readOnly = true)
    fun getMissionGroupDetail(userId: Long, groupId: Long): MissionGroupResponse {
        val group = missionGroupRepository.findById(groupId)
            .orElseThrow { BusinessException(ErrorCode.MISSION_GROUP_NOT_FOUND) }

        val missions = missionRepository.findByMissionGroupId(groupId)
        val userMissionMap = getUserMissionMap(userId, missions)

        return MissionGroupResponse(
            id = group.id,
            name = group.name,
            description = group.description,
            type = group.type,
            startDate = group.startDate,
            endDate = group.endDate,
            imageUrl = group.imageUrl,
            missions = missions.map { mission ->
                toMissionResponse(mission, userMissionMap[mission.id])
            }
        )
    }

    // 내 미션 현황: 달성/진행 카운트 + 최근 달성 목록
    @Transactional(readOnly = true)
    fun getMyMissionSummary(userId: Long): MyMissionSummaryResponse {
        val achievedCount = userMissionRepository.countByUserIdAndStatus(userId, MissionStatus.ACHIEVED)
        val inProgressCount = userMissionRepository.countByUserIdAndStatus(userId, MissionStatus.IN_PROGRESS)

        val recentAchievements = userMissionRepository
            .findByUserIdAndStatusOrderByAchievedAtDesc(userId, MissionStatus.ACHIEVED)
            .take(10)

        val missionIds = recentAchievements.map { it.missionId }
        val missions = if (missionIds.isNotEmpty()) {
            missionRepository.findAllById(missionIds).associateBy { it.id }
        } else emptyMap()

        // 그룹명 조회
        val groupIds = missions.values.map { it.missionGroupId }.distinct()
        val groups = if (groupIds.isNotEmpty()) {
            missionGroupRepository.findAllById(groupIds).associateBy { it.id }
        } else emptyMap()

        // 전체 활성 미션 수
        val allGroups = missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()
        val allGroupIds = allGroups.map { it.id }
        val totalMissions = if (allGroupIds.isNotEmpty()) {
            missionRepository.findByMissionGroupIdIn(allGroupIds).size
        } else 0

        val achievedMissions = recentAchievements.mapNotNull { userMission ->
            val mission = missions[userMission.missionId] ?: return@mapNotNull null
            val group = groups[mission.missionGroupId]
            AchievedMissionResponse(
                missionId = mission.id,
                missionName = mission.name,
                missionImageUrl = mission.imageUrl,
                groupName = group?.name ?: "",
                conditionType = mission.conditionType,
                achievedAt = userMission.achievedAt
            )
        }

        return MyMissionSummaryResponse(
            totalMissions = totalMissions,
            achievedCount = achievedCount,
            inProgressCount = inProgressCount,
            recentAchievements = achievedMissions
        )
    }

    private fun getUserMissionMap(userId: Long, missions: List<Mission>): Map<Long, UserMission> {
        if (missions.isEmpty()) return emptyMap()
        val missionIds = missions.map { it.id }
        return userMissionRepository.findByUserIdAndMissionIdIn(userId, missionIds)
            .associateBy { it.missionId }
    }

    private fun toMissionResponse(mission: Mission, userMission: UserMission?): MissionResponse {
        return MissionResponse(
            id = mission.id,
            name = mission.name,
            description = mission.description,
            imageUrl = mission.imageUrl,
            conditionType = mission.conditionType,
            conditionValue = mission.conditionValue,
            conditionStartDate = mission.conditionStartDate,
            conditionEndDate = mission.conditionEndDate,
            status = userMission?.status ?: MissionStatus.NOT_ACHIEVED,
            currentValue = userMission?.currentValue ?: 0
        )
    }
}
