package com.runnershi.domain.ranking.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.domain.ranking.dto.MyRankingResponse
import com.runnershi.domain.ranking.dto.RankingListResponse
import com.runnershi.domain.ranking.dto.RankingResponse
import com.runnershi.domain.ranking.service.RankingService
import com.runnershi.domain.user.entity.Tier
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RankingControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var rankingService: RankingService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private lateinit var accessToken: String

    @BeforeEach
    fun setUp() {
        accessToken = jwtTokenProvider.createAccessToken(1L)
    }

    @Nested
    @DisplayName("GET /api/rankings")
    inner class GetRankings {

        @Test
        @DisplayName("랭킹 목록 조회 성공")
        fun success() {
            val rankings = listOf(
                RankingResponse(1, 2L, "1등러너", null, Tier.GOLD, 10000),
                RankingResponse(2, 1L, "나", null, Tier.BRONZE, 5000)
            )
            val response = RankingListResponse(rankings, null, false)
            whenever(rankingService.getRankings(any(), anyOrNull(), any())).thenReturn(response)

            mockMvc.perform(
                get("/api/rankings")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rankings.length()").value(2))
                .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }

        @Test
        @DisplayName("지역 미선택 시 에러 반환")
        fun regionNotSelected() {
            whenever(rankingService.getRankings(any(), anyOrNull(), any()))
                .thenThrow(BusinessException(ErrorCode.REGION_NOT_SELECTED))

            mockMvc.perform(
                get("/api/rankings")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.error.code").value("U005"))
        }

        @Test
        @DisplayName("인증 없이 요청 시 에러 반환")
        fun withoutAuth() {
            mockMvc.perform(get("/api/rankings"))
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("GET /api/rankings/me")
    inner class GetMyRanking {

        @Test
        @DisplayName("내 순위 조회 성공")
        fun success() {
            val response = MyRankingResponse(3, "나", null, Tier.SILVER, 5000)
            whenever(rankingService.getMyRanking(any())).thenReturn(response)

            mockMvc.perform(
                get("/api/rankings/me")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.rank").value(3))
                .andExpect(jsonPath("$.data.nickname").value("나"))
                .andExpect(jsonPath("$.data.tier").value("SILVER"))
        }
    }
}
