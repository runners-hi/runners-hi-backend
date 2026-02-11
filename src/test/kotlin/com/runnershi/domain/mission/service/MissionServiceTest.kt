package com.runnershi.domain.mission.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.Mission
import com.runnershi.domain.mission.entity.MissionGroup
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import com.runnershi.domain.mission.repository.MissionGroupRepository
import com.runnershi.domain.mission.repository.MissionRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MissionServiceTest {

    @Mock lateinit var missionGroupRepository: MissionGroupRepository
    @Mock lateinit var missionRepository: MissionRepository
    @Mock lateinit var userMissionRepository: UserMissionRepository

    private lateinit var missionService: MissionService

    @BeforeEach
    fun setUp() {
        missionService = MissionService(missionGroupRepository, missionRepository, userMissionRepository)
    }

    private fun createMissionGroup(id: Long, name: String, type: MissionGroupType, showOnHome: Boolean = false): MissionGroup {
        val group = MissionGroup(name = name, type = type, showOnHome = showOnHome)
        ReflectionTestUtils.setField(group, "id", id)
        return group
    }

    private fun createMission(id: Long, groupId: Long, name: String, conditionType: ConditionType = ConditionType.SINGLE_DISTANCE, conditionValue: Int? = 5000): Mission {
        val mission = Mission(missionGroupId = groupId, name = name, conditionType = conditionType, conditionValue = conditionValue)
        ReflectionTestUtils.setField(mission, "id", id)
        return mission
    }

    private fun createUserMission(userId: Long, missionId: Long, status: MissionStatus, currentValue: Int = 0): UserMission {
        return UserMission(userId = userId, missionId = missionId, status = status, currentValue = currentValue,
            achievedAt = if (status == MissionStatus.ACHIEVED) LocalDateTime.now() else null)
    }

    @Nested
    @DisplayName("getMissionGroups")
    inner class GetMissionGroups {

        @Test
        @DisplayName("미션 그룹 목록 조회 성공 - 그룹별 미션 + 유저 상태 포함")
        fun success() {
            val group = createMissionGroup(1L, "웰컴 미션", MissionGroupType.WELCOME)
            val mission1 = createMission(1L, 1L, "첫 러닝", ConditionType.FIRST_RUN, null)
            val mission2 = createMission(2L, 1L, "5km 러닝", ConditionType.SINGLE_DISTANCE, 5000)
            val userMission = createUserMission(1L, 1L, MissionStatus.ACHIEVED)

            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(listOf(group))
            whenever(missionRepository.findByMissionGroupIdIn(listOf(1L))).thenReturn(listOf(mission1, mission2))
            whenever(userMissionRepository.findByUserIdAndMissionIdIn(1L, listOf(1L, 2L))).thenReturn(listOf(userMission))

            val result = missionService.getMissionGroups(1L)

            assertEquals(1, result.size)
            assertEquals("웰컴 미션", result[0].name)
            assertEquals(2, result[0].missions.size)
            assertEquals(MissionStatus.ACHIEVED, result[0].missions[0].status)
            assertEquals(MissionStatus.NOT_ACHIEVED, result[0].missions[1].status)
        }

        @Test
        @DisplayName("그룹이 없으면 빈 목록 반환")
        fun emptyGroups() {
            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(emptyList())

            val result = missionService.getMissionGroups(1L)

            assertEquals(0, result.size)
        }

        @Test
        @DisplayName("여러 그룹 + 미션 매핑 정확성")
        fun multipleGroups() {
            val group1 = createMissionGroup(1L, "웰컴", MissionGroupType.WELCOME)
            val group2 = createMissionGroup(2L, "이벤트", MissionGroupType.EVENT)
            val mission1 = createMission(1L, 1L, "미션A")
            val mission2 = createMission(2L, 2L, "미션B")

            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(listOf(group1, group2))
            whenever(missionRepository.findByMissionGroupIdIn(listOf(1L, 2L))).thenReturn(listOf(mission1, mission2))
            whenever(userMissionRepository.findByUserIdAndMissionIdIn(any(), any())).thenReturn(emptyList())

            val result = missionService.getMissionGroups(1L)

            assertEquals(2, result.size)
            assertEquals(1, result[0].missions.size)
            assertEquals("미션A", result[0].missions[0].name)
            assertEquals(1, result[1].missions.size)
            assertEquals("미션B", result[1].missions[0].name)
        }
    }

    @Nested
    @DisplayName("getHomeMissions")
    inner class GetHomeMissions {

        @Test
        @DisplayName("홈 미션 조회 성공")
        fun success() {
            val group = createMissionGroup(1L, "할로윈 이벤트", MissionGroupType.EVENT, showOnHome = true)
            val mission = createMission(1L, 1L, "할로윈 러닝")

            whenever(missionGroupRepository.findActiveForHome(any())).thenReturn(listOf(group))
            whenever(missionRepository.findByMissionGroupId(1L)).thenReturn(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionIdIn(any(), any())).thenReturn(emptyList())

            val result = missionService.getHomeMissions(1L)

            assertNotNull(result)
            assertEquals("할로윈 이벤트", result.groupName)
            assertEquals(1, result.missions.size)
        }

        @Test
        @DisplayName("활성 홈 미션이 없으면 null 반환")
        fun noActiveMission() {
            whenever(missionGroupRepository.findActiveForHome(any())).thenReturn(emptyList())

            val result = missionService.getHomeMissions(1L)

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("getMissionGroupDetail")
    inner class GetMissionGroupDetail {

        @Test
        @DisplayName("미션 그룹 상세 조회 성공")
        fun success() {
            val group = createMissionGroup(1L, "웰컴 미션", MissionGroupType.WELCOME)
            val mission = createMission(1L, 1L, "첫 러닝", ConditionType.FIRST_RUN, null)
            val userMission = createUserMission(1L, 1L, MissionStatus.ACHIEVED)

            whenever(missionGroupRepository.findById(1L)).thenReturn(Optional.of(group))
            whenever(missionRepository.findByMissionGroupId(1L)).thenReturn(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionIdIn(1L, listOf(1L))).thenReturn(listOf(userMission))

            val result = missionService.getMissionGroupDetail(1L, 1L)

            assertEquals("웰컴 미션", result.name)
            assertEquals(MissionGroupType.WELCOME, result.type)
            assertEquals(1, result.missions.size)
            assertEquals(MissionStatus.ACHIEVED, result.missions[0].status)
        }

        @Test
        @DisplayName("존재하지 않는 그룹 - 예외 발생")
        fun notFound() {
            whenever(missionGroupRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                missionService.getMissionGroupDetail(1L, 999L)
            }
            assertEquals(ErrorCode.MISSION_GROUP_NOT_FOUND, exception.errorCode)
        }
    }

    @Nested
    @DisplayName("getMyMissionSummary")
    inner class GetMyMissionSummary {

        @Test
        @DisplayName("내 미션 현황 조회 성공")
        fun success() {
            val group = createMissionGroup(1L, "웰컴", MissionGroupType.WELCOME)
            val mission = createMission(1L, 1L, "첫 러닝", ConditionType.FIRST_RUN, null)
            ReflectionTestUtils.setField(mission, "id", 1L)
            val userMission = createUserMission(1L, 1L, MissionStatus.ACHIEVED)

            whenever(userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.ACHIEVED)).thenReturn(3)
            whenever(userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.IN_PROGRESS)).thenReturn(1)
            whenever(userMissionRepository.findByUserIdAndStatusOrderByAchievedAtDesc(1L, MissionStatus.ACHIEVED))
                .thenReturn(listOf(userMission))
            whenever(missionRepository.findAllById(listOf(1L))).thenReturn(listOf(mission))
            whenever(missionGroupRepository.findAllById(listOf(1L))).thenReturn(listOf(group))
            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(listOf(group))
            whenever(missionRepository.findByMissionGroupIdIn(listOf(1L))).thenReturn(listOf(mission))

            val result = missionService.getMyMissionSummary(1L)

            assertEquals(3, result.achievedCount)
            assertEquals(1, result.inProgressCount)
            assertEquals(1, result.totalMissions)
            assertEquals(1, result.recentAchievements.size)
            assertEquals("첫 러닝", result.recentAchievements[0].missionName)
        }

        @Test
        @DisplayName("달성 미션이 없으면 빈 목록 반환")
        fun noAchievements() {
            whenever(userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.ACHIEVED)).thenReturn(0)
            whenever(userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.IN_PROGRESS)).thenReturn(0)
            whenever(userMissionRepository.findByUserIdAndStatusOrderByAchievedAtDesc(1L, MissionStatus.ACHIEVED))
                .thenReturn(emptyList())
            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(emptyList())

            val result = missionService.getMyMissionSummary(1L)

            assertEquals(0, result.achievedCount)
            assertEquals(0, result.inProgressCount)
            assertEquals(0, result.totalMissions)
            assertEquals(0, result.recentAchievements.size)
        }
    }
}
