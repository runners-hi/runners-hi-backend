package com.runnershi.domain.notification.service

import com.runnershi.domain.notification.entity.Notification
import com.runnershi.domain.notification.entity.NotificationType
import com.runnershi.domain.notification.repository.NotificationRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock lateinit var notificationRepository: NotificationRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var fcmService: FcmService

    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        notificationService = NotificationService(notificationRepository, userRepository, fcmService)
    }

    private fun createUser(
        notificationEnabled: Boolean = true,
        fcmToken: String? = "token123"
    ): User = User(
        provider = Provider.KAKAO,
        providerId = "test-provider-id",
        nickname = "테스트러너",
        notificationEnabled = notificationEnabled,
        fcmToken = fcmToken
    )

    @Nested
    @DisplayName("sendMissionAchievedNotification")
    inner class SendMissionAchievedNotification {

        @Test
        @DisplayName("인앱 알림 저장 확인")
        fun savesInAppNotification() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            notificationService.sendMissionAchievedNotification(1L, "5km 달리기")

            val captor = ArgumentCaptor.forClass(Notification::class.java)
            verify(notificationRepository).save(captor.capture())

            val saved = captor.value
            assertEquals(1L, saved.userId)
            assertEquals(NotificationType.MISSION_ACHIEVED, saved.type)
            assertEquals("미션 달성!", saved.title)
            assertTrue(saved.body.contains("5km 달리기"))
        }

        @Test
        @DisplayName("알림 허용 + FCM 토큰 있으면 FCM 전송")
        fun sendsFcm_whenNotificationEnabledAndTokenPresent() {
            val user = createUser(notificationEnabled = true, fcmToken = "token123")
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            notificationService.sendMissionAchievedNotification(1L, "10km 달리기")

            verify(fcmService).sendPush("token123", "미션 달성!", "\"10km 달리기\" 미션을 달성했습니다")
        }

        @Test
        @DisplayName("알림 비허용이면 FCM 미전송")
        fun doesNotSendFcm_whenNotificationDisabled() {
            val user = createUser(notificationEnabled = false, fcmToken = "token123")
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            notificationService.sendMissionAchievedNotification(1L, "테스트 미션")

            verify(fcmService, never()).sendPush(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())
        }

        @Test
        @DisplayName("FCM 토큰 없으면 FCM 미전송")
        fun doesNotSendFcm_whenFcmTokenIsNull() {
            val user = createUser(notificationEnabled = true, fcmToken = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            notificationService.sendMissionAchievedNotification(1L, "테스트 미션")

            verify(fcmService, never()).sendPush(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any())
        }
    }

    @Nested
    @DisplayName("getUnreadCount")
    inner class GetUnreadCount {

        @Test
        @DisplayName("미읽은 알림 수 반환")
        fun returnsUnreadCount() {
            whenever(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L)

            val result = notificationService.getUnreadCount(1L)

            assertEquals(3L, result)
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    inner class MarkAllAsRead {

        @Test
        @DisplayName("미읽은 알림 전체 읽음 처리")
        fun marksAllUnreadNotificationsAsRead() {
            val notification1 = Notification(
                userId = 1L,
                type = NotificationType.MISSION_ACHIEVED,
                title = "미션 달성!",
                body = "테스트 미션 달성"
            )
            val notification2 = Notification(
                userId = 1L,
                type = NotificationType.MISSION_ACHIEVED,
                title = "미션 달성!",
                body = "두 번째 미션 달성"
            )
            whenever(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(listOf(notification1, notification2))

            notificationService.markAllAsRead(1L)

            assertTrue(notification1.isRead)
            assertTrue(notification2.isRead)
        }
    }
}
