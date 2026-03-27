package com.runnershi.domain.user.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.entity.RegionType
import com.runnershi.domain.user.dto.MyProfileResponse
import com.runnershi.domain.user.service.UserService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class UserControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("PATCH /api/users/nickname")
    inner class UpdateNickname {

        @Test
        @DisplayName("닉네임 변경 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/users/nickname")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("nickname" to "새닉네임")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("빈 닉네임이면 400 반환")
        fun blankNickname_returns400() {
            mockMvc.perform(
                patch("/api/users/nickname")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("nickname" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("중복 닉네임이면 409 반환")
        fun duplicateNickname_returns409() {
            doThrow(BusinessException(ErrorCode.DUPLICATE_NICKNAME))
                .whenever(userService).updateNickname(any(), any())

            mockMvc.perform(
                patch("/api/users/nickname")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("nickname" to "중복닉네임")))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("U003"))
        }
    }

    @Nested
    @DisplayName("GET /api/users/nickname/check")
    inner class CheckNickname {

        @Test
        @DisplayName("사용 가능한 닉네임")
        fun available() {
            whenever(userService.checkNicknameAvailable("새닉네임")).thenReturn(true)

            mockMvc.perform(
                get("/api/users/nickname/check")
                    .header("Authorization", "Bearer $accessToken")
                    .param("nickname", "새닉네임")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
        }

        @Test
        @DisplayName("이미 사용 중인 닉네임")
        fun notAvailable() {
            whenever(userService.checkNicknameAvailable("중복닉네임")).thenReturn(false)

            mockMvc.perform(
                get("/api/users/nickname/check")
                    .header("Authorization", "Bearer $accessToken")
                    .param("nickname", "중복닉네임")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.available").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/users/me")
    inner class GetMyProfile {

        @Test
        @DisplayName("내 프로필 조회 성공")
        fun success() {
            val profileResponse = MyProfileResponse(
                nickname = "테스트러너",
                profileImageUrl = null,
                region = RegionResponse(1L, "서울특별시", RegionType.SPECIAL_CITY),
                notificationEnabled = true,
                marketingNotificationEnabled = false
            )
            whenever(userService.getMyProfile(any())).thenReturn(profileResponse)

            mockMvc.perform(
                get("/api/users/me")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("테스트러너"))
                .andExpect(jsonPath("$.data.region.name").value("서울특별시"))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/region")
    inner class UpdateRegion {

        @Test
        @DisplayName("지역 변경 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/users/region")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("regionId" to 1)))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/me")
    inner class Withdraw {

        @Test
        @DisplayName("계정 탈퇴 성공")
        fun success() {
            mockMvc.perform(
                delete("/api/users/me")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/fcm-token")
    inner class UpdateFcmToken {

        @Test
        @DisplayName("FCM 토큰 등록 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/users/fcm-token")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("fcmToken" to "test-fcm-token-123")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("빈 FCM 토큰이면 400 반환")
        fun blankFcmToken_returns400() {
            mockMvc.perform(
                patch("/api/users/fcm-token")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("fcmToken" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/notification-settings")
    inner class UpdateNotificationSettings {

        @Test
        @DisplayName("알림 설정 변경 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/users/notification-settings")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "notificationEnabled" to true,
                        "marketingNotificationEnabled" to false
                    )))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("notificationEnabled 누락 시 400")
        fun missingFields_returns400() {
            mockMvc.perform(
                patch("/api/users/notification-settings")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(emptyMap<String, Any>()))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/profile-image")
    inner class UpdateProfileImage {

        @Test
        @DisplayName("프로필 이미지 URL 저장 성공")
        fun success() {
            mockMvc.perform(
                patch("/api/users/profile-image")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("profileImageUrl" to "https://storage.googleapis.com/bucket/image.jpg")))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
        }

        @Test
        @DisplayName("빈 URL이면 400")
        fun blankUrl_returns400() {
            mockMvc.perform(
                patch("/api/users/profile-image")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf("profileImageUrl" to "")))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Test
    @DisplayName("인증 없이 API 호출 시 에러 반환")
    fun withoutAuth_returnsError() {
        mockMvc.perform(get("/api/users/me"))
            .andExpect(status().isForbidden)
    }
}
