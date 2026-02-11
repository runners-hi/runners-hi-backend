package com.runnershi.domain.mission.service

import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.Mission
import com.runnershi.domain.mission.entity.MissionGroup
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import com.runnershi.domain.mission.repository.MissionGroupRepository
import com.runnershi.domain.mission.repository.MissionRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.running.entity.RunningRecord
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MissionCheckerTest {

    @Mock lateinit var missionGroupRepository: MissionGroupRepository
    @Mock lateinit var missionRepository: MissionRepository
    @Mock lateinit var userMissionRepository: UserMissionRepository
    @Mock lateinit var userRepository: UserRepository

    private lateinit var missionChecker: MissionChecker

    @BeforeEach
    fun setUp() {
        missionChecker = MissionChecker(missionGroupRepository, missionRepository, userMissionRepository, userRepository)
    }

    private fun createMissionGroup(id: Long, type: MissionGroupType): MissionGroup {
        val group = MissionGroup(name = "테스트 그룹", type = type)
        ReflectionTestUtils.setField(group, "id", id)
        return group
    }

    private fun createMission(
        id: Long,
        groupId: Long,
        conditionType: ConditionType,
        conditionValue: Int? = null,
        conditionStartDate: LocalDate? = null,
        conditionEndDate: LocalDate? = null
    ): Mission {
        val mission = Mission(
            missionGroupId = groupId,
            name = "테스트 미션",
            conditionType = conditionType,
            conditionValue = conditionValue,
            conditionStartDate = conditionStartDate,
            conditionEndDate = conditionEndDate
        )
        ReflectionTestUtils.setField(mission, "id", id)
        return mission
    }

    private fun createRunningRecord(distance: Int, pace: Int, runningDate: LocalDate = LocalDate.now()): RunningRecord {
        return RunningRecord(
            userId = 1L,
            runningDate = runningDate,
            distance = distance,
            duration = pace * (distance / 1000),
            pace = pace,
            startedAt = runningDate.atTime(7, 0),
            endedAt = runningDate.atTime(7, 30)
        )
    }

    private fun stubActiveMissions(missions: List<Mission>) {
        val group = createMissionGroup(1L, MissionGroupType.WELCOME)
        whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(listOf(group))
        whenever(missionRepository.findByMissionGroupIdIn(listOf(1L))).thenReturn(missions)
        whenever(userMissionRepository.findByUserIdAndMissionIdIn(any(), any())).thenReturn(emptyList())
    }

    @Nested
    @DisplayName("checkOnRunningRecordCreated")
    inner class CheckOnRunningRecordCreated {

        @Test
        @DisplayName("SINGLE_DISTANCE - 거리 충족 시 달성")
        fun singleDistance_achieved() {
            val mission = createMission(1L, 1L, ConditionType.SINGLE_DISTANCE, conditionValue = 5000)
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 5000, pace = 360)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("SINGLE_DISTANCE - 거리 미달 시 미달성")
        fun singleDistance_notAchieved() {
            val mission = createMission(1L, 1L, ConditionType.SINGLE_DISTANCE, conditionValue = 5000)
            stubActiveMissions(listOf(mission))

            val record = createRunningRecord(distance = 3000, pace = 360)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }

        @Test
        @DisplayName("CUMULATIVE_DISTANCE - 누적 거리 충족 시 달성")
        fun cumulativeDistance_achieved() {
            val mission = createMission(1L, 1L, ConditionType.CUMULATIVE_DISTANCE, conditionValue = 10000)
            stubActiveMissions(listOf(mission))

            val user = User(provider = Provider.KAKAO, providerId = "kakao-1", nickname = "러너")
            user.totalDistance = 10000
            ReflectionTestUtils.setField(user, "id", 1L)

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 5000, pace = 360)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("CUMULATIVE_DISTANCE - 누적 거리 미달 시 IN_PROGRESS")
        fun cumulativeDistance_inProgress() {
            val mission = createMission(1L, 1L, ConditionType.CUMULATIVE_DISTANCE, conditionValue = 10000)
            stubActiveMissions(listOf(mission))

            val user = User(provider = Provider.KAKAO, providerId = "kakao-1", nickname = "러너")
            user.totalDistance = 5000
            ReflectionTestUtils.setField(user, "id", 1L)

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 3000, pace = 360)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("PACE - 페이스 조건 충족 시 달성")
        fun pace_achieved() {
            val mission = createMission(1L, 1L, ConditionType.PACE, conditionValue = 360) // 6분/km 이하
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 5000, pace = 300) // 5분/km
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("PACE - 페이스 미달 시 미달성")
        fun pace_notAchieved() {
            val mission = createMission(1L, 1L, ConditionType.PACE, conditionValue = 300) // 5분/km 이하
            stubActiveMissions(listOf(mission))

            val record = createRunningRecord(distance = 5000, pace = 360) // 6분/km
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }

        @Test
        @DisplayName("RUN_ON_DATE - 해당 날짜에 러닝 시 달성")
        fun runOnDate_achieved() {
            val targetDate = LocalDate.of(2025, 1, 1)
            val mission = createMission(1L, 1L, ConditionType.RUN_ON_DATE, conditionStartDate = targetDate)
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 5000, pace = 360, runningDate = targetDate)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("RUN_ON_DATE - 다른 날짜에 러닝 시 미달성")
        fun runOnDate_notAchieved() {
            val targetDate = LocalDate.of(2025, 1, 1)
            val mission = createMission(1L, 1L, ConditionType.RUN_ON_DATE, conditionStartDate = targetDate)
            stubActiveMissions(listOf(mission))

            val record = createRunningRecord(distance = 5000, pace = 360, runningDate = LocalDate.of(2025, 1, 2))
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }

        @Test
        @DisplayName("RUN_IN_PERIOD - 기간 내 러닝 시 달성")
        fun runInPeriod_achieved() {
            val startDate = LocalDate.of(2025, 1, 1)
            val endDate = LocalDate.of(2025, 1, 31)
            val mission = createMission(1L, 1L, ConditionType.RUN_IN_PERIOD, conditionStartDate = startDate, conditionEndDate = endDate)
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 5000, pace = 360, runningDate = LocalDate.of(2025, 1, 15))
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("RUN_IN_PERIOD - 기간 외 러닝 시 미달성")
        fun runInPeriod_notAchieved() {
            val startDate = LocalDate.of(2025, 1, 1)
            val endDate = LocalDate.of(2025, 1, 31)
            val mission = createMission(1L, 1L, ConditionType.RUN_IN_PERIOD, conditionStartDate = startDate, conditionEndDate = endDate)
            stubActiveMissions(listOf(mission))

            val record = createRunningRecord(distance = 5000, pace = 360, runningDate = LocalDate.of(2025, 2, 1))
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }

        @Test
        @DisplayName("FIRST_RUN - 첫 러닝 시 달성")
        fun firstRun_achieved() {
            val mission = createMission(1L, 1L, ConditionType.FIRST_RUN)
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            val record = createRunningRecord(distance = 1000, pace = 600)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("이미 달성한 미션은 스킵")
        fun alreadyAchieved_skip() {
            val mission = createMission(1L, 1L, ConditionType.FIRST_RUN)
            val group = createMissionGroup(1L, MissionGroupType.WELCOME)
            val achievedUserMission = UserMission(userId = 1L, missionId = 1L, status = MissionStatus.ACHIEVED, achievedAt = LocalDateTime.now())

            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(listOf(group))
            whenever(missionRepository.findByMissionGroupIdIn(listOf(1L))).thenReturn(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionIdIn(1L, listOf(1L))).thenReturn(listOf(achievedUserMission))

            val record = createRunningRecord(distance = 1000, pace = 600)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }

        @Test
        @DisplayName("활성 미션이 없으면 아무것도 하지 않음")
        fun noActiveMissions() {
            whenever(missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()).thenReturn(emptyList())

            val record = createRunningRecord(distance = 5000, pace = 360)
            missionChecker.checkOnRunningRecordCreated(1L, record)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }
    }

    @Nested
    @DisplayName("checkOnRegionSet")
    inner class CheckOnRegionSet {

        @Test
        @DisplayName("REGION_SET 미션 달성")
        fun regionSet_achieved() {
            val mission = createMission(1L, 1L, ConditionType.REGION_SET)
            stubActiveMissions(listOf(mission))
            whenever(userMissionRepository.findByUserIdAndMissionId(1L, 1L)).thenReturn(null)

            missionChecker.checkOnRegionSet(1L)

            verify(userMissionRepository).save(any<UserMission>())
        }

        @Test
        @DisplayName("REGION_SET 미션이 없으면 아무것도 하지 않음")
        fun noRegionSetMission() {
            val mission = createMission(1L, 1L, ConditionType.FIRST_RUN) // REGION_SET이 아님
            stubActiveMissions(listOf(mission))

            missionChecker.checkOnRegionSet(1L)

            verify(userMissionRepository, never()).save(any<UserMission>())
        }
    }
}
