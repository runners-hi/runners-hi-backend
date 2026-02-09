package com.runnershi.domain.mission.service

import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.Mission
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import com.runnershi.domain.mission.repository.MissionGroupRepository
import com.runnershi.domain.mission.repository.MissionRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.running.entity.RunningRecord
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

// === 미션 달성 체크 ===
// - 러닝 기록 저장 시: SINGLE_DISTANCE, CUMULATIVE_DISTANCE, PACE, RUN_ON_DATE, RUN_IN_PERIOD, FIRST_RUN 체크
// - 지역 설정 시: REGION_SET 체크
// - 이미 달성한 미션은 스킵, 활성 그룹(WELCOME + 기간 내 EVENT)의 미션만 체크
// TODO: 미션 달성 시 알림 발송 (알림 시스템 구현 후)
@Service
class MissionChecker(
    private val missionGroupRepository: MissionGroupRepository,
    private val missionRepository: MissionRepository,
    private val userMissionRepository: UserMissionRepository,
    private val userRepository: UserRepository
) {

    // 러닝 기록 저장 시 호출
    @Transactional
    fun checkOnRunningRecordCreated(userId: Long, record: RunningRecord) {
        val activeMissions = getActiveMissionsNotAchieved(userId)

        for (mission in activeMissions) {
            when (mission.conditionType) {
                ConditionType.SINGLE_DISTANCE -> checkSingleDistance(userId, mission, record)
                ConditionType.CUMULATIVE_DISTANCE -> checkCumulativeDistance(userId, mission)
                ConditionType.PACE -> checkPace(userId, mission, record)
                ConditionType.RUN_ON_DATE -> checkRunOnDate(userId, mission, record)
                ConditionType.RUN_IN_PERIOD -> checkRunInPeriod(userId, mission, record)
                ConditionType.FIRST_RUN -> achieveMission(userId, mission)
                ConditionType.REGION_SET -> { /* checkOnRegionSet에서 별도 처리 */ }
            }
        }
    }

    // 지역 설정 시 호출
    @Transactional
    fun checkOnRegionSet(userId: Long) {
        val activeMissions = getActiveMissionsNotAchieved(userId)
        activeMissions
            .filter { it.conditionType == ConditionType.REGION_SET }
            .forEach { achieveMission(userId, it) }
    }

    // 활성 그룹의 미달성 미션 조회
    // - WELCOME: 항상 활성
    // - EVENT: 현재 날짜가 startDate~endDate 범위 내
    private fun getActiveMissionsNotAchieved(userId: Long): List<Mission> {
        val today = LocalDate.now()
        val groups = missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()
        val activeGroups = groups.filter { group ->
            group.type == MissionGroupType.WELCOME ||
            (group.startDate != null && group.endDate != null &&
             !today.isBefore(group.startDate) && !today.isAfter(group.endDate))
        }
        if (activeGroups.isEmpty()) return emptyList()

        val missions = missionRepository.findByMissionGroupIdIn(activeGroups.map { it.id })
        if (missions.isEmpty()) return emptyList()

        val userMissions = userMissionRepository.findByUserIdAndMissionIdIn(userId, missions.map { it.id })
        val achievedMissionIds = userMissions
            .filter { it.status == MissionStatus.ACHIEVED }
            .map { it.missionId }
            .toSet()

        return missions.filter { it.id !in achievedMissionIds }
    }

    // 1회 러닝 거리 체크
    private fun checkSingleDistance(userId: Long, mission: Mission, record: RunningRecord) {
        val requiredDistance = mission.conditionValue ?: return
        if (record.distance >= requiredDistance) {
            achieveMission(userId, mission)
        }
    }

    // 누적 거리 체크 (User.totalDistance 기준, 진행률 추적)
    private fun checkCumulativeDistance(userId: Long, mission: Mission) {
        val requiredDistance = mission.conditionValue ?: return
        val user = userRepository.findById(userId).orElse(null) ?: return

        val userMission = getOrCreateUserMission(userId, mission.id)
        userMission.currentValue = user.totalDistance

        if (user.totalDistance >= requiredDistance) {
            userMission.status = MissionStatus.ACHIEVED
            userMission.achievedAt = LocalDateTime.now()
        } else {
            userMission.status = MissionStatus.IN_PROGRESS
        }
        userMissionRepository.save(userMission)
    }

    // 페이스 체크 (낮을수록 빠름, conditionValue 이하면 달성)
    private fun checkPace(userId: Long, mission: Mission, record: RunningRecord) {
        val requiredPace = mission.conditionValue ?: return
        if (record.pace <= requiredPace) {
            achieveMission(userId, mission)
        }
    }

    // 특정 날짜 러닝 체크
    private fun checkRunOnDate(userId: Long, mission: Mission, record: RunningRecord) {
        val targetDate = mission.conditionStartDate ?: return
        if (record.runningDate == targetDate) {
            achieveMission(userId, mission)
        }
    }

    // 특정 기간 내 러닝 체크
    private fun checkRunInPeriod(userId: Long, mission: Mission, record: RunningRecord) {
        val startDate = mission.conditionStartDate ?: return
        val endDate = mission.conditionEndDate ?: return
        if (!record.runningDate.isBefore(startDate) && !record.runningDate.isAfter(endDate)) {
            achieveMission(userId, mission)
        }
    }

    private fun achieveMission(userId: Long, mission: Mission) {
        val userMission = getOrCreateUserMission(userId, mission.id)
        if (userMission.status == MissionStatus.ACHIEVED) return

        userMission.status = MissionStatus.ACHIEVED
        userMission.achievedAt = LocalDateTime.now()
        mission.conditionValue?.let { userMission.currentValue = it }
        userMissionRepository.save(userMission)
    }

    private fun getOrCreateUserMission(userId: Long, missionId: Long): UserMission {
        return userMissionRepository.findByUserIdAndMissionId(userId, missionId)
            ?: UserMission(userId = userId, missionId = missionId)
    }
}
