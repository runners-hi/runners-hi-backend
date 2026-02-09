package com.runnershi.domain.mission.repository

import com.runnershi.domain.mission.entity.Mission
import org.springframework.data.jpa.repository.JpaRepository

interface MissionRepository : JpaRepository<Mission, Long> {

    fun findByMissionGroupId(missionGroupId: Long): List<Mission>

    fun findByMissionGroupIdIn(missionGroupIds: List<Long>): List<Mission>
}
