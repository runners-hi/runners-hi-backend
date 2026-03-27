package com.runnershi.domain.notification.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class FcmService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendPush(fcmToken: String, title: String, body: String) {
        try {
            val message = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .build()

            val messageId = FirebaseMessaging.getInstance().send(message)
            log.debug("FCM 전송 성공: messageId={}", messageId)
        } catch (e: Exception) {
            // FCM 실패는 알림 누락일 뿐이므로 트랜잭션 롤백 없이 경고 로그
            log.warn("FCM 전송 실패: token={}, error={}", fcmToken.take(20), e.message)
        }
    }
}
