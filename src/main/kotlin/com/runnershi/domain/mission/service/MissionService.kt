package com.runnershi.domain.mission.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.mission.dto.HomeMissionResponse
import com.runnershi.domain.mission.dto.MissionGroupResponse
import com.runnershi.domain.mission.dto.MissionResponse
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
