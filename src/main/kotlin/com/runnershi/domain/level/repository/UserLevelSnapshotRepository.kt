package com.runnershi.domain.level.repository

import com.runnershi.domain.level.entity.UserLevelSnapshot
import org.springframework.data.jpa.repository.JpaRepository

interface UserLevelSnapshotRepository : JpaRepository<UserLevelSnapshot, Long> {

    // 특정 유저의 특정 연도 마지막 스냅샷 (최종 티어 확인용)
    fun findFirstByUserIdAndYearOrderByMonthDesc(userId: Long, year: Int): UserLevelSnapshot?

    // 특정 유저의 연도별 스냅샷 목록
    fun findByUserIdAndYearOrderByMonthAsc(userId: Long, year: Int): List<UserLevelSnapshot>
}
