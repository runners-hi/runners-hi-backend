package com.runnershi.domain.user.repository

import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): User?
    fun existsByNickname(nickname: String): Boolean

    // === 랭킹 쿼리 ===
    // 정렬: totalDistance DESC, id ASC (동점 시 가입순)
    // 대상: 해당 지역의 ACTIVE 유저만

    @Query("""
        SELECT u FROM User u
        WHERE u.regionId = :regionId
        AND u.status = :status
        ORDER BY u.totalDistance DESC, u.id ASC
    """)
    fun findRanking(regionId: Long, status: UserStatus, pageable: Pageable): List<User>

    // 커서 기반: (totalDistance, id) 기준 keyset pagination
    @Query("""
        SELECT u FROM User u
        WHERE u.regionId = :regionId
        AND u.status = :status
        AND (u.totalDistance < :cursorDistance
             OR (u.totalDistance = :cursorDistance AND u.id > :cursorId))
        ORDER BY u.totalDistance DESC, u.id ASC
    """)
    fun findRankingWithCursor(
        regionId: Long,
        status: UserStatus,
        cursorDistance: Int,
        cursorId: Long,
        pageable: Pageable
    ): List<User>

    // 순위 계산: 나보다 totalDistance가 높은 유저 수 + 1 = 내 순위
    // 동점 처리: 같은 거리면 같은 순위, 다음 순위는 건너뜀 (RANK 방식)
    @Query("""
        SELECT COUNT(u) + 1 FROM User u
        WHERE u.regionId = :regionId
        AND u.status = :status
        AND u.totalDistance > :totalDistance
    """)
    fun countRank(regionId: Long, status: UserStatus, totalDistance: Int): Long
}
