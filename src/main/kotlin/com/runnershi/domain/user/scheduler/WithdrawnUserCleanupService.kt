package com.runnershi.domain.user.scheduler

import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class WithdrawnUserCleanupService(
    private val runningRecordRepository: RunningRecordRepository,
    private val userMissionRepository: UserMissionRepository,
    private val userLevelSnapshotRepository: UserLevelSnapshotRepository,
    private val termsAgreementRepository: TermsAgreementRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun cleanupUser(user: User) {
        val userId = user.id

        // 연관 데이터 삭제
        runningRecordRepository.deleteAllByUserId(userId)
        userMissionRepository.deleteAllByUserId(userId)
        userLevelSnapshotRepository.deleteAllByUserId(userId)
        termsAgreementRepository.deleteAllByUserId(userId)

        // 개인정보 마스킹
        user.nickname = "탈퇴한사용자_${UUID.randomUUID()}"
        user.email = null
        user.profileImageUrl = null
        user.fcmToken = null
        user.providerId = "deleted_${UUID.randomUUID()}"
        user.status = UserStatus.DELETED

        log.info("유저 정리 완료: userId={}", userId)
    }
}
