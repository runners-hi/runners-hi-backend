package com.runnershi.domain.notification.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.notification.dto.NotificationResponse
import com.runnershi.domain.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Notification", description = "인앱 알림 API")
@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    fun getNotifications(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<List<NotificationResponse>> {
        val notifications = notificationService.getNotifications(userId)
        return ApiResponse.success(notifications.map { NotificationResponse.from(it) })
    }

    @Operation(summary = "미읽은 알림 수 조회")
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Long> {
        return ApiResponse.success(notificationService.getUnreadCount(userId))
    }

    @Operation(summary = "알림 전체 읽음 처리")
    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Unit> {
        notificationService.markAllAsRead(userId)
        return ApiResponse.success(Unit)
    }
}
