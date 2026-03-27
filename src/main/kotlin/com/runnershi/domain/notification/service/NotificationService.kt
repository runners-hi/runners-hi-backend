package com.runnershi.domain.notification.service

import com.runnershi.domain.notification.entity.Notification
import com.runnershi.domain.notification.entity.NotificationType
import com.runnershi.domain.notification.repository.NotificationRepository
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val fcmService: FcmService
) {

    // 미션 달성 알림 발송 (FCM Push + In-App)
    @Transactional
    fun sendMissionAchievedNotification(userId: Long, missionTitle: String) {
        val title = "미션 달성!"
        val body = "\"$missionTitle\" 미션을 달성했습니다"

        // In-App 알림 저장
        notificationRepository.save(
            Notification(
                userId = userId,
                type = NotificationType.MISSION_ACHIEVED,
                title = title,
                body = body
            )
        )

        // FCM Push (알림 허용 + fcmToken 있는 경우만)
        val user = userRepository.findById(userId).orElse(null) ?: return
        if (user.notificationEnabled && user.fcmToken != null) {
            fcmService.sendPush(user.fcmToken!!, title, body)
        }
    }

    // 미읽은 알림 수 조회
    @Transactional(readOnly = true)
    fun getUnreadCount(userId: Long): Long {
        return notificationRepository.countByUserIdAndIsReadFalse(userId)
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    fun getNotifications(userId: Long): List<Notification> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
    }

    // 알림 읽음 처리
    @Transactional
    fun markAllAsRead(userId: Long) {
        val unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
        unread.forEach { it.isRead = true }
    }
}
