package com.runnershi.domain.level.service

import com.runnershi.domain.level.repository.LevelConfigRepository
import com.runnershi.domain.user.entity.Tier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LevelService(
    private val levelConfigRepository: LevelConfigRepository
) {

    // === 티어 기준 (스크린샷 확정) ===
    // Bronze:   Level 1 - 5
    // Silver:   Level 6 - 20
    // Gold:     Level 21 - 40
    // Platinum: Level 41 - 70
    // Diamond:  Level 71 - 100

    // === 정책 ===
    // - 경험치(experience) → level_config 테이블에서 레벨 결정
    // - 레벨 → 아래 tierForLevel()로 티어 결정
    // - 경험치 획득 공식은 RunningRecordService 참고 (1km당 100XP, 수동 입력 0.7배, 하루 상한 1200XP)
    // - 연초 초기화는 하지 않는다. 레벨이 누적 경험치에서 계산되므로 초기화하면 레벨도 떨어진다 (BE-090)

    companion object {
        private const val MAX_LEVEL = 100
    }

    fun tierForLevel(level: Int): Tier = when (level) {
        in 1..5 -> Tier.BRONZE
        in 6..20 -> Tier.SILVER
        in 21..40 -> Tier.GOLD
        in 41..70 -> Tier.PLATINUM
        in 71..MAX_LEVEL -> Tier.DIAMOND
        else -> Tier.BRONZE
    }

    // 경험치로 레벨 계산
    @Transactional(readOnly = true)
    fun calculateLevel(experience: Int): Int {
        return levelConfigRepository.findMaxLevelByExperience(experience)?.level ?: 1
    }

    // 경험치로 레벨 + 티어 한번에 계산
    @Transactional(readOnly = true)
    fun calculateLevelAndTier(experience: Int): Pair<Int, Tier> {
        val level = calculateLevel(experience)
        val tier = tierForLevel(level)
        return Pair(level, tier)
    }

    // 다음 레벨까지 진행률 (%) - 프론트에서 Progress bar용
    @Transactional(readOnly = true)
    fun calculateProgress(experience: Int): Int {
        val currentLevel = calculateLevel(experience)
        if (currentLevel >= MAX_LEVEL) return 100

        val currentConfig = levelConfigRepository.findByLevel(currentLevel) ?: return 0
        val nextConfig = levelConfigRepository.findByLevel(currentLevel + 1) ?: return 100

        val currentXp = experience - currentConfig.requiredExperience
        val neededXp = nextConfig.requiredExperience - currentConfig.requiredExperience

        return if (neededXp > 0) (currentXp * 100 / neededXp) else 100
    }
}
