package com.runnershi.domain.notification.dto

import com.runnershi.domain.notification.entity.Notification
import com.runnershi.domain.notification.entity.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            type = notification.type,
            title = notification.title,
            body = notification.body,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}
