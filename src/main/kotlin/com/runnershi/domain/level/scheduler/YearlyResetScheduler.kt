package com.runnershi.domain.level.scheduler

import com.runnershi.domain.level.service.UserLevelSnapshotService
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

// BE-090+091: 연초 경험치 초기화 스케줄러
//
// 정책:
// - 매년 1월 1일 00:00 KST에 ACTIVE 유저의 경험치를 0으로 리셋
// - 레벨/티어는 유지 (경험치만 초기화)
// - 초기화 직전 전년도 12월 스냅샷 저장 (이력 보존)
//
// 이유: 경험치 리셋으로 매년 새로운 동기부여 제공,
//       레벨/티어 유지로 기존 유저의 업적 보호
@Component
class YearlyResetScheduler(
    private val userRepository: UserRepository,
    private val userLevelSnapshotService: UserLevelSnapshotService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    // 매년 1월 1일 00:00 KST (UTC+9 = UTC 전날 15:00)
    @Scheduled(cron = "0 0 15 31 12 *", zone = "UTC")
    @Transactional
    fun resetYearlyExperience() {
        val now = LocalDate.now()
        val targetYear = now.year

        log.info("연초 경험치 초기화 시작 - year={}", targetYear + 1)

        val activeUsers = userRepository.findAllByStatus(UserStatus.ACTIVE)
        if (activeUsers.isEmpty()) {
            log.info("초기화 대상 유저 없음")
            return
        }

        // 전년도 연말 스냅샷 저장
        userLevelSnapshotService.saveYearEndSnapshots(activeUsers, targetYear)

        // 경험치 초기화 (레벨/티어 유지)
        activeUsers.forEach { user -> user.experience = 0 }

        log.info("연초 경험치 초기화 완료 - 대상 유저 수={}", activeUsers.size)
    }
}
