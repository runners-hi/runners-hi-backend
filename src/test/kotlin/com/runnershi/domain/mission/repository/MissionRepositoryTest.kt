package com.runnershi.domain.mission.repository

import com.runnershi.domain.mission.entity.ConditionType
import com.runnershi.domain.mission.entity.Mission
import com.runnershi.domain.mission.entity.MissionGroup
import com.runnershi.domain.mission.entity.MissionGroupType
import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MissionRepositoryTest : RepositoryTest() {

    @Autowired lateinit var missionGroupRepository: MissionGroupRepository
    @Autowired lateinit var missionRepository: MissionRepository
    @Autowired lateinit var userMissionRepository: UserMissionRepository

    @Nested
    @DisplayName("MissionGroupRepository")
    inner class MissionGroupRepo {

        @BeforeEach
        fun setUp() {
            missionGroupRepository.saveAll(listOf(
                MissionGroup(name = "웰컴 미션", type = MissionGroupType.WELCOME, showOnHome = true),
                MissionGroup(name = "할로윈 이벤트", type = MissionGroupType.EVENT,
                    startDate = LocalDate.of(2025, 10, 1), endDate = LocalDate.of(2025, 10, 31), showOnHome = true),
                MissionGroup(name = "지난 이벤트", type = MissionGroupType.EVENT,
                    startDate = LocalDate.of(2024, 1, 1), endDate = LocalDate.of(2024, 1, 31), showOnHome = false)
            ))
        }

        @Test
        @DisplayName("전체 그룹 조회 - type ASC, createdAt DESC 정렬")
        fun findAll() {
            val result = missionGroupRepository.findAllByOrderByTypeAscCreatedAtDesc()

            assertEquals(3, result.size)
            // EVENT가 먼저 (type ASC: EVENT < WELCOME)
            assertEquals(MissionGroupType.EVENT, result[0].type)
        }

        @Test
        @DisplayName("홈 활성 미션 조회 - showOnHome=true + 기간 내")
        fun findActiveForHome() {
            val result = missionGroupRepository.findActiveForHome(LocalDate.of(2025, 10, 15))

            // WELCOME(항상) + 할로윈(기간 내) = 2개, 지난 이벤트는 showOnHome=false라 제외
            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("홈 활성 미션 - 기간 외 이벤트 제외")
        fun findActiveForHome_expired() {
            val result = missionGroupRepository.findActiveForHome(LocalDate.of(2025, 12, 1))

            // WELCOME만 포함 (할로윈 기간 지남)
            assertEquals(1, result.size)
            assertEquals(MissionGroupType.WELCOME, result[0].type)
        }
    }

    @Nested
    @DisplayName("MissionRepository")
    inner class MissionRepo {

        private var groupId: Long = 0L

        @BeforeEach
        fun setUp() {
            val group = missionGroupRepository.save(MissionGroup(name = "테스트 그룹", type = MissionGroupType.WELCOME))
            groupId = group.id

            missionRepository.saveAll(listOf(
                Mission(missionGroupId = groupId, name = "첫 러닝", conditionType = ConditionType.FIRST_RUN),
                Mission(missionGroupId = groupId, name = "5km 달리기", conditionType = ConditionType.SINGLE_DISTANCE, conditionValue = 5000),
                Mission(missionGroupId = groupId, name = "누적 10km", conditionType = ConditionType.CUMULATIVE_DISTANCE, conditionValue = 10000)
            ))
        }

        @Test
        @DisplayName("그룹별 미션 조회")
        fun findByMissionGroupId() {
            val result = missionRepository.findByMissionGroupId(groupId)

            assertEquals(3, result.size)
        }

        @Test
        @DisplayName("여러 그룹의 미션 일괄 조회")
        fun findByMissionGroupIdIn() {
            val group2 = missionGroupRepository.save(MissionGroup(name = "이벤트", type = MissionGroupType.EVENT))
            missionRepository.save(Mission(missionGroupId = group2.id, name = "이벤트 미션", conditionType = ConditionType.RUN_ON_DATE))

            val result = missionRepository.findByMissionGroupIdIn(listOf(groupId, group2.id))

            assertEquals(4, result.size)
        }

        @Test
        @DisplayName("빈 그룹 조회 시 빈 목록")
        fun findByMissionGroupId_empty() {
            val result = missionRepository.findByMissionGroupId(999L)

            assertEquals(0, result.size)
        }
    }

    @Nested
    @DisplayName("UserMissionRepository")
    inner class UserMissionRepo {

        private var missionId1: Long = 0L
        private var missionId2: Long = 0L

        @BeforeEach
        fun setUp() {
            val group = missionGroupRepository.save(MissionGroup(name = "테스트", type = MissionGroupType.WELCOME))
            val m1 = missionRepository.save(Mission(missionGroupId = group.id, name = "미션1", conditionType = ConditionType.FIRST_RUN))
            val m2 = missionRepository.save(Mission(missionGroupId = group.id, name = "미션2", conditionType = ConditionType.SINGLE_DISTANCE, conditionValue = 5000))
            missionId1 = m1.id
            missionId2 = m2.id

            userMissionRepository.saveAll(listOf(
                UserMission(userId = 1L, missionId = missionId1, status = MissionStatus.ACHIEVED, achievedAt = LocalDateTime.of(2025, 1, 10, 12, 0)),
                UserMission(userId = 1L, missionId = missionId2, status = MissionStatus.IN_PROGRESS, currentValue = 3000)
            ))
        }

        @Test
        @DisplayName("유저의 미션 상태 일괄 조회")
        fun findByUserIdAndMissionIdIn() {
            val result = userMissionRepository.findByUserIdAndMissionIdIn(1L, listOf(missionId1, missionId2))

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("유저의 특정 미션 상태 조회")
        fun findByUserIdAndMissionId() {
            val result = userMissionRepository.findByUserIdAndMissionId(1L, missionId1)

            assertNotNull(result)
            assertEquals(MissionStatus.ACHIEVED, result.status)
        }

        @Test
        @DisplayName("존재하지 않는 미션 조회 시 null")
        fun findByUserIdAndMissionId_notFound() {
            val result = userMissionRepository.findByUserIdAndMissionId(1L, 999L)

            assertNull(result)
        }

        @Test
        @DisplayName("상태별 카운트 조회")
        fun countByUserIdAndStatus() {
            val achievedCount = userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.ACHIEVED)
            val inProgressCount = userMissionRepository.countByUserIdAndStatus(1L, MissionStatus.IN_PROGRESS)

            assertEquals(1, achievedCount)
            assertEquals(1, inProgressCount)
        }

        @Test
        @DisplayName("달성 미션 최신순 조회")
        fun findAchievedOrderByAchievedAtDesc() {
            val result = userMissionRepository.findByUserIdAndStatusOrderByAchievedAtDesc(1L, MissionStatus.ACHIEVED)

            assertEquals(1, result.size)
            assertEquals(missionId1, result[0].missionId)
        }

        @Test
        @DisplayName("다른 유저의 미션 조회 시 빈 목록")
        fun otherUser() {
            val result = userMissionRepository.findByUserIdAndMissionIdIn(999L, listOf(missionId1))

            assertEquals(0, result.size)
        }
    }
}
