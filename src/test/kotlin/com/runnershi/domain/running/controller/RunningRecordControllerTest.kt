package com.runnershi.domain.running.controller

import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.running.dto.DailyRecordResponse
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.dto.WeeklySummaryResponse
import com.runnershi.domain.running.service.RunningRecordService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

class RunningRecordControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var runningRecordService: RunningRecordService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    private val sampleRecord = RunningRecordResponse(
        id = 1L,
        runningDate = LocalDate.of(2025, 1, 6),
        distance = 5000,
        duration = 1800,
        pace = 360,
        calories = 300,
        startedAt = LocalDateTime.of(2025, 1, 6, 7, 0),
        endedAt = LocalDateTime.of(2025, 1, 6, 7, 30),
        memo = null,
        heartRateAvg = null,
        heartRateMax = null,
        elevationGain = null,
        elevationLoss = null,
        runningType = null
    )

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("POST /api/running-records")
    inner class CreateRunningRecord {

        @Test
        @DisplayName("러닝 기록 저장 성공")
        fun success() {
            whenever(runningRecordService.createRunningRecord(any(), any())).thenReturn(sampleRecord)

            mockMvc.perform(
                post("/api/running-records")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "distance" to 5000,
                        "duration" to 1800,
                        "calories" to 300,
                        "startedAt" to "2025-01-06T07:00:00",
                        "endedAt" to "2025-01-06T07:30:00",
                        "memo" to "아침 러닝"
                    )))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.distance").value(5000))
                .andExpect(jsonPath("$.data.pace").value(360))
        }

        @Test
        @DisplayName("거리 0 이하이면 400 반환")
        fun invalidDistance_returns400() {
            mockMvc.perform(
                post("/api/running-records")
                    .header("Authorization", "Bearer $accessToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "distance" to 0,
                        "duration" to 1800,
                        "startedAt" to "2025-01-06T07:00:00",
                        "endedAt" to "2025-01-06T07:30:00"
                    )))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        @DisplayName("인증 없이 요청 시 에러 반환")
        fun withoutAuth() {
            mockMvc.perform(
                post("/api/running-records")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(mapOf(
                        "distance" to 5000,
                        "duration" to 1800,
                        "startedAt" to "2025-01-06T07:00:00",
                        "endedAt" to "2025-01-06T07:30:00"
                    )))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("GET /api/running-records/weekly")
    inner class GetWeeklySummary {

        @Test
        @DisplayName("주간 요약 조회 성공")
        fun success() {
            val summary = WeeklySummaryResponse(
                startDate = LocalDate.of(2025, 1, 6),
                endDate = LocalDate.of(2025, 1, 12),
                totalDistance = 15000L,
                totalDuration = 5400,
                averagePace = 360,
                runCount = 3,
                runDays = 3
            )
            whenever(runningRecordService.getWeeklySummary(any(), any())).thenReturn(summary)

            mockMvc.perform(
                get("/api/running-records/weekly")
                    .header("Authorization", "Bearer $accessToken")
                    .param("date", "2025-01-08")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalDistance").value(15000))
                .andExpect(jsonPath("$.data.runDays").value(3))
        }
    }

    @Nested
    @DisplayName("GET /api/running-records/daily")
    inner class GetDailyRecord {

        @Test
        @DisplayName("일일 기록 조회 성공")
        fun success() {
            val daily = DailyRecordResponse(
                date = LocalDate.of(2025, 1, 6),
                totalDistance = 5000,
                totalDuration = 1800,
                averagePace = 360,
                runCount = 1,
                records = listOf(sampleRecord)
            )
            whenever(runningRecordService.getDailyRecord(any(), any())).thenReturn(daily)

            mockMvc.perform(
                get("/api/running-records/daily")
                    .header("Authorization", "Bearer $accessToken")
                    .param("date", "2025-01-06")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.totalDistance").value(5000))
                .andExpect(jsonPath("$.data.runCount").value(1))
        }
    }

    @Nested
    @DisplayName("GET /api/running-records")
    inner class GetRunningRecords {

        @Test
        @DisplayName("기록 목록 조회 성공")
        fun success() {
            val listResponse = RunningRecordListResponse(
                records = listOf(sampleRecord),
                nextCursor = null,
                hasNext = false
            )
            whenever(runningRecordService.getRunningRecords(any(), anyOrNull(), any())).thenReturn(listResponse)

            mockMvc.perform(
                get("/api/running-records")
                    .header("Authorization", "Bearer $accessToken")
                    .param("size", "20")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.records.length()").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }
    }
}
