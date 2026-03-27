package com.runnershi.domain.level.service

import com.runnershi.domain.level.entity.UserLevelSnapshot
import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.user.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserLevelSnapshotService(
    private val userLevelSnapshotRepository: UserLevelSnapshotRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 유저 목록의 연말 스냅샷 일괄 저장 (이미 존재하면 스킵)
    @Transactional
    fun saveYearEndSnapshots(users: List<User>, year: Int) {
        val month = 12
        val snapshots = users.mapNotNull { user ->
            val exists = userLevelSnapshotRepository
                .findFirstByUserIdAndYearOrderByMonthDesc(user.id, year)
                ?.month == month

            if (exists) {
                log.debug("스냅샷 이미 존재 - userId={}, year={}, month={}", user.id, year, month)
                null
            } else {
                UserLevelSnapshot(
                    userId = user.id,
                    year = year,
                    month = month,
                    level = user.level,
                    tier = user.tier,
                    experience = user.experience,
                    totalDistance = user.totalDistance
                )
            }
        }

        if (snapshots.isNotEmpty()) {
            userLevelSnapshotRepository.saveAll(snapshots)
            log.info("연말 스냅샷 저장 완료 - year={}, count={}", year, snapshots.size)
        }
    }
}
