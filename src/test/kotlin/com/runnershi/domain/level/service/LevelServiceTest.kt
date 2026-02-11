package com.runnershi.domain.level.service

import com.runnershi.domain.level.entity.LevelConfig
import com.runnershi.domain.level.repository.LevelConfigRepository
import com.runnershi.domain.user.entity.Tier
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LevelServiceTest {

    @Mock lateinit var levelConfigRepository: LevelConfigRepository

    private lateinit var levelService: LevelService

    @BeforeEach
    fun setUp() {
        levelService = LevelService(levelConfigRepository)
    }

    @Nested
    @DisplayName("tierForLevel")
    inner class TierForLevel {

        @Test
        @DisplayName("레벨 1~5 → BRONZE")
        fun bronze() {
            assertEquals(Tier.BRONZE, levelService.tierForLevel(1))
            assertEquals(Tier.BRONZE, levelService.tierForLevel(5))
        }

        @Test
        @DisplayName("레벨 6~20 → SILVER")
        fun silver() {
            assertEquals(Tier.SILVER, levelService.tierForLevel(6))
            assertEquals(Tier.SILVER, levelService.tierForLevel(20))
        }

        @Test
        @DisplayName("레벨 21~40 → GOLD")
        fun gold() {
            assertEquals(Tier.GOLD, levelService.tierForLevel(21))
            assertEquals(Tier.GOLD, levelService.tierForLevel(40))
        }

        @Test
        @DisplayName("레벨 41~70 → PLATINUM")
        fun platinum() {
            assertEquals(Tier.PLATINUM, levelService.tierForLevel(41))
            assertEquals(Tier.PLATINUM, levelService.tierForLevel(70))
        }

        @Test
        @DisplayName("레벨 71~100 → DIAMOND")
        fun diamond() {
            assertEquals(Tier.DIAMOND, levelService.tierForLevel(71))
            assertEquals(Tier.DIAMOND, levelService.tierForLevel(100))
        }

        @Test
        @DisplayName("범위 밖 → BRONZE")
        fun outOfRange() {
            assertEquals(Tier.BRONZE, levelService.tierForLevel(0))
            assertEquals(Tier.BRONZE, levelService.tierForLevel(101))
        }
    }

    @Nested
    @DisplayName("calculateLevel")
    inner class CalculateLevel {

        @Test
        @DisplayName("경험치에 해당하는 최대 레벨 반환")
        fun success() {
            whenever(levelConfigRepository.findMaxLevelByExperience(500))
                .thenReturn(LevelConfig(level = 5, requiredExperience = 400))

            assertEquals(5, levelService.calculateLevel(500))
        }

        @Test
        @DisplayName("레벨 설정 없으면 1 반환")
        fun noConfig() {
            whenever(levelConfigRepository.findMaxLevelByExperience(0)).thenReturn(null)

            assertEquals(1, levelService.calculateLevel(0))
        }
    }

    @Nested
    @DisplayName("calculateLevelAndTier")
    inner class CalculateLevelAndTier {

        @Test
        @DisplayName("경험치로 레벨과 티어 동시 계산")
        fun success() {
            whenever(levelConfigRepository.findMaxLevelByExperience(1000))
                .thenReturn(LevelConfig(level = 10, requiredExperience = 900))

            val (level, tier) = levelService.calculateLevelAndTier(1000)

            assertEquals(10, level)
            assertEquals(Tier.SILVER, tier) // 레벨 10 → SILVER
        }
    }

    @Nested
    @DisplayName("calculateProgress")
    inner class CalculateProgress {

        @Test
        @DisplayName("다음 레벨까지 진행률 계산")
        fun progress() {
            whenever(levelConfigRepository.findMaxLevelByExperience(550))
                .thenReturn(LevelConfig(level = 5, requiredExperience = 400))
            whenever(levelConfigRepository.findByLevel(5))
                .thenReturn(LevelConfig(level = 5, requiredExperience = 400))
            whenever(levelConfigRepository.findByLevel(6))
                .thenReturn(LevelConfig(level = 6, requiredExperience = 600))

            // (550 - 400) / (600 - 400) = 150 / 200 = 75%
            assertEquals(75, levelService.calculateProgress(550))
        }

        @Test
        @DisplayName("최대 레벨이면 100% 반환")
        fun maxLevel() {
            whenever(levelConfigRepository.findMaxLevelByExperience(99999))
                .thenReturn(LevelConfig(level = 100, requiredExperience = 90000))

            assertEquals(100, levelService.calculateProgress(99999))
        }

        @Test
        @DisplayName("레벨 설정 없으면 0% 반환")
        fun noCurrentConfig() {
            whenever(levelConfigRepository.findMaxLevelByExperience(50))
                .thenReturn(LevelConfig(level = 1, requiredExperience = 0))
            whenever(levelConfigRepository.findByLevel(1)).thenReturn(null)

            assertEquals(0, levelService.calculateProgress(50))
        }
    }
}
