package com.runnershi.domain.level.repository

import com.runnershi.domain.level.entity.LevelConfig
import com.runnershi.domain.level.entity.UserLevelSnapshot
import com.runnershi.domain.user.entity.Tier
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LevelRepositoryTest : RepositoryTest() {

    @Autowired lateinit var levelConfigRepository: LevelConfigRepository
    @Autowired lateinit var userLevelSnapshotRepository: UserLevelSnapshotRepository

    @Nested
    @DisplayName("LevelConfigRepository")
    inner class LevelConfigRepo {

        @BeforeEach
        fun setUp() {
            levelConfigRepository.saveAll(listOf(
                LevelConfig(level = 1, requiredExperience = 0),
                LevelConfig(level = 2, requiredExperience = 100),
                LevelConfig(level = 3, requiredExperience = 300),
                LevelConfig(level = 4, requiredExperience = 600),
                LevelConfig(level = 5, requiredExperience = 1000)
            ))
        }

        @Test
        @DisplayName("경험치에 해당하는 최대 레벨 조회")
        fun findMaxLevelByExperience() {
            val result = levelConfigRepository.findMaxLevelByExperience(500)

            assertNotNull(result)
            assertEquals(3, result.level) // 300 <= 500 < 600
        }

        @Test
        @DisplayName("경험치 0이면 레벨 1")
        fun zeroExperience() {
            val result = levelConfigRepository.findMaxLevelByExperience(0)

            assertNotNull(result)
            assertEquals(1, result.level)
        }

        @Test
        @DisplayName("정확히 경계값인 경우")
        fun exactBoundary() {
            val result = levelConfigRepository.findMaxLevelByExperience(300)

            assertNotNull(result)
            assertEquals(3, result.level)
        }

        @Test
        @DisplayName("레벨로 설정 조회")
        fun findByLevel() {
            val result = levelConfigRepository.findByLevel(3)

            assertNotNull(result)
            assertEquals(300, result.requiredExperience)
        }

        @Test
        @DisplayName("존재하지 않는 레벨 조회 시 null")
        fun findByLevel_notFound() {
            assertNull(levelConfigRepository.findByLevel(99))
        }
    }

    @Nested
    @DisplayName("UserLevelSnapshotRepository")
    inner class UserLevelSnapshotRepo {

        @BeforeEach
        fun setUp() {
            userLevelSnapshotRepository.saveAll(listOf(
                UserLevelSnapshot(userId = 1L, year = 2025, month = 1, level = 5, tier = Tier.BRONZE, experience = 1000, totalDistance = 5000),
                UserLevelSnapshot(userId = 1L, year = 2025, month = 6, level = 15, tier = Tier.SILVER, experience = 5000, totalDistance = 20000),
                UserLevelSnapshot(userId = 1L, year = 2025, month = 12, level = 25, tier = Tier.GOLD, experience = 10000, totalDistance = 50000),
                UserLevelSnapshot(userId = 1L, year = 2024, month = 12, level = 3, tier = Tier.BRONZE, experience = 500, totalDistance = 2000)
            ))
        }

        @Test
        @DisplayName("특정 연도의 마지막 스냅샷 조회 (월 내림차순 첫 번째)")
        fun findLastSnapshot() {
            val result = userLevelSnapshotRepository.findFirstByUserIdAndYearOrderByMonthDesc(1L, 2025)

            assertNotNull(result)
            assertEquals(12, result.month)
            assertEquals(Tier.GOLD, result.tier)
        }

        @Test
        @DisplayName("특정 연도의 스냅샷 목록 조회 (월 오름차순)")
        fun findByYear() {
            val result = userLevelSnapshotRepository.findByUserIdAndYearOrderByMonthAsc(1L, 2025)

            assertEquals(3, result.size)
            assertEquals(1, result[0].month)
            assertEquals(6, result[1].month)
            assertEquals(12, result[2].month)
        }

        @Test
        @DisplayName("스냅샷 없는 연도 조회 시 빈 목록")
        fun noSnapshots() {
            val result = userLevelSnapshotRepository.findByUserIdAndYearOrderByMonthAsc(1L, 2023)

            assertEquals(0, result.size)
        }
    }
}
