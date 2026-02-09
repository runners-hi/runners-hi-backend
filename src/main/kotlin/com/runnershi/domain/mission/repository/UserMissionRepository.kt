package com.runnershi.domain.mission.repository

import com.runnershi.domain.mission.entity.MissionStatus
import com.runnershi.domain.mission.entity.UserMission
import org.springframework.data.jpa.repository.JpaRepository

interface UserMissionRepository : JpaRepository<UserMission, Long> {

    fun findByUserIdAndMissionIdIn(userId: Long, missionIds: List<Long>): List<UserMission>

    fun findByUserIdAndMissionId(userId: Long, missionId: Long): UserMission?

    fun findByUserId(userId: Long): List<UserMission>

    fun countByUserIdAndStatus(userId: Long, status: MissionStatus): Int

    fun findByUserIdAndStatusOrderByAchievedAtDesc(userId: Long, status: MissionStatus): List<UserMission>
}
