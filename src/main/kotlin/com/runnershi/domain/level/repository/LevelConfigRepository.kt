package com.runnershi.domain.level.repository

import com.runnershi.domain.level.entity.LevelConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface LevelConfigRepository : JpaRepository<LevelConfig, Int> {

    // 해당 경험치로 도달 가능한 최대 레벨 조회
    @Query("""
        SELECT lc FROM LevelConfig lc
        WHERE lc.requiredExperience <= :experience
        ORDER BY lc.level DESC
        LIMIT 1
    """)
    fun findMaxLevelByExperience(experience: Int): LevelConfig?

    // 다음 레벨의 필요 경험치 조회 (진행률 계산용)
    fun findByLevel(level: Int): LevelConfig?
}
