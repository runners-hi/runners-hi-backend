package com.runnershi.domain.notification.controller

import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.notification.entity.Notification
import com.runnershi.domain.notification.entity.NotificationType
import com.runnershi.domain.notification.service.NotificationService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class NotificationControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var notificationService: NotificationService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("GET /api/notifications")
    inner class GetNotifications {

        @Test
        @DisplayName("알림 목록 조회 성공")
        fun success() {
            val notification1 = Notification(
                userId = 1L,
                type = NotificationType.MISSION_ACHIEVED,
                title = "미션 달성!",
                body = "\"5km 달리기\" 미션을 달성했습니다"
            )
            val notification2 = Notification(
                userId = 1L,
                type = NotificationType.LEVEL_UP,
                title = "레벨업!",
                body = "레벨 5로 올랐습니다",
                isRead = true
            )
            whenever(notificationService.getNotifications(1L)).thenReturn(listOf(notification1, notification2))

            mockMvc.perform(
                get("/api/notifications")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("미션 달성!"))
                .andExpect(jsonPath("$.data[1].title").value("레벨업!"))
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread-count")
    inner class GetUnreadCount {

        @Test
        @DisplayName("미읽은 알림 수 반환")
        fun success() {
            whenever(notificationService.getUnreadCount(1L)).thenReturn(5L)

            mockMvc.perform(
                get("/api/notifications/unread-count")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(5))
        }
    }

    @Nested
    @DisplayName("PATCH /api/notifications/read-all")
    inner class MarkAllAsRead {

        @Test
        @DisplayName("전체 읽음 처리 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/notifications/read-all")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Test
    @DisplayName("인증 없이 GET /api/notifications - 403 반환")
    fun withoutAuth_returnsForbidden() {
        mockMvc.perform(get("/api/notifications"))
            .andExpect(status().isForbidden)
    }
}
