package com.runnershi.domain.user.scheduler

import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class WithdrawnUserCleanupScheduler(
    private val userRepository: UserRepository,
    private val runningRecordRepository: RunningRecordRepository,
    private val userMissionRepository: UserMissionRepository,
    private val userLevelSnapshotRepository: UserLevelSnapshotRepository,
    private val termsAgreementRepository: TermsAgreementRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val GRACE_PERIOD_DAYS = 30L
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
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
                cleanupUser(user)
            } catch (e: Exception) {
                log.error("유저 정리 실패: userId={}, error={}", user.id, e.message, e)
            }
        }

        log.info("탈퇴 유저 정리 완료: {}명 처리", users.size)
    }

    private fun cleanupUser(user: User) {
        val userId = user.id

        // 연관 데이터 삭제
        runningRecordRepository.deleteAllByUserId(userId)
        userMissionRepository.deleteAllByUserId(userId)
        userLevelSnapshotRepository.deleteAllByUserId(userId)
        termsAgreementRepository.deleteAllByUserId(userId)

        // 개인정보 마스킹
        user.nickname = "탈퇴한사용자_${UUID.randomUUID().toString().substring(0, 8)}"
        user.email = null
        user.profileImageUrl = null
        user.fcmToken = null
        user.providerId = "deleted_${UUID.randomUUID()}"
        user.status = UserStatus.DELETED

        log.info("유저 정리 완료: userId={}", userId)
    }
}
