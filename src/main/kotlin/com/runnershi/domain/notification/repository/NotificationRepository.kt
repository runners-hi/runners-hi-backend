package com.runnershi.domain.notification.repository

import com.runnershi.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId: Long): List<Notification>
    fun findTop100ByUserIdOrderByCreatedAtDesc(userId: Long): List<Notification>
    fun countByUserIdAndIsReadFalse(userId: Long): Long
    fun deleteAllByUserId(userId: Long)
}
