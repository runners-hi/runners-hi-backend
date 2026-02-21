package com.runnershi.domain.user.scheduler

import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class WithdrawnUserCleanupScheduler(
    private val userRepository: UserRepository,
    private val cleanupService: WithdrawnUserCleanupService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val GRACE_PERIOD_DAYS = 30L
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanupWithdrawnUsers() {
        val cutoff = LocalDateTime.now().minusDays(GRACE_PERIOD_DAYS)
        val users = userRepository.findByStatusAndDeletedAtBefore(UserStatus.WITHDRAWN, cutoff)

        if (users.isEmpty()) {
            log.info("정리 대상 탈퇴 유저 없음")
            return
        }

        log.info("탈퇴 유저 정리 시작: {}명", users.size)

        users.forEach { user ->
            try {
                cleanupService.cleanupUser(user)
            } catch (e: Exception) {
                log.error("유저 정리 실패: userId={}, error={}", user.id, e.message, e)
            }
        }

        log.info("탈퇴 유저 정리 완료: {}명 처리", users.size)
    }
}
