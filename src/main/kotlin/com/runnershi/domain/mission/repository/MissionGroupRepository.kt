package com.runnershi.domain.mission.repository

import com.runnershi.domain.mission.entity.MissionGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface MissionGroupRepository : JpaRepository<MissionGroup, Long> {

    // 미션 탭: 전체 그룹 (WELCOME 먼저, 이벤트는 최신순)
    fun findAllByOrderByTypeAscCreatedAtDesc(): List<MissionGroup>

    // 홈 화면: showOnHome=true이고 현재 진행 중인 이벤트 (WELCOME 포함)
    @Query(
        "SELECT mg FROM MissionGroup mg " +
        "WHERE mg.showOnHome = true " +
        "AND (mg.type = 'WELCOME' OR (mg.startDate <= :today AND mg.endDate >= :today)) " +
        "ORDER BY mg.type DESC, mg.createdAt DESC"
    )
    fun findActiveForHome(@Param("today") today: LocalDate): List<MissionGroup>
}
