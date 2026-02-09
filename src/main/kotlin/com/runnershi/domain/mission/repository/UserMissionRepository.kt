package com.runnershi.domain.mission.repository

import com.runnershi.domain.mission.entity.UserMission
import org.springframework.data.jpa.repository.JpaRepository

interface UserMissionRepository : JpaRepository<UserMission, Long> {

    fun findByUserIdAndMissionIdIn(userId: Long, missionIds: List<Long>): List<UserMission>

    fun findByUserIdAndMissionId(userId: Long, missionId: Long): UserMission?
}
