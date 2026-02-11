package com.runnershi.domain.app.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.app.dto.AppVersionCheckResponse
import com.runnershi.domain.app.service.AppVersionService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AppVersionControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var appVersionService: AppVersionService

    @Nested
    @DisplayName("GET /api/app/version/check")
    inner class CheckVersion {

        @Test
        @DisplayName("강제 업데이트 응답")
        fun forceUpdate() {
            val response = AppVersionCheckResponse(
                forceUpdate = true, recommendUpdate = false,
                latestVersion = "2.0.0", updateUrl = "https://store.example.com"
            )
            whenever(appVersionService.checkVersion(any(), any())).thenReturn(response)

            mockMvc.perform(
                get("/api/app/version/check")
                    .param("platform", "IOS")
                    .param("version", "1.0.0")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.forceUpdate").value(true))
                .andExpect(jsonPath("$.data.recommendUpdate").value(false))
                .andExpect(jsonPath("$.data.latestVersion").value("2.0.0"))
        }

        @Test
        @DisplayName("최신 버전 응답")
        fun upToDate() {
            val response = AppVersionCheckResponse(
                forceUpdate = false, recommendUpdate = false,
                latestVersion = "2.0.0", updateUrl = null
            )
            whenever(appVersionService.checkVersion(any(), any())).thenReturn(response)

            mockMvc.perform(
                get("/api/app/version/check")
                    .param("platform", "ANDROID")
                    .param("version", "2.0.0")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.forceUpdate").value(false))
                .andExpect(jsonPath("$.data.recommendUpdate").value(false))
        }

        @Test
        @DisplayName("지원하지 않는 플랫폼 - 에러 반환")
        fun platformNotFound() {
            whenever(appVersionService.checkVersion(any(), any()))
                .thenThrow(BusinessException(ErrorCode.PLATFORM_NOT_FOUND))

            mockMvc.perform(
                get("/api/app/version/check")
                    .param("platform", "IOS")
                    .param("version", "1.0.0")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error.code").value("AP001"))
        }

        @Test
        @DisplayName("필수 파라미터 누락 시 에러")
        fun missingParams() {
            mockMvc.perform(get("/api/app/version/check"))
                .andExpect(status().isBadRequest)
        }
    }
}
