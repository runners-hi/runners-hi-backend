package com.runnershi.domain.ranking.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.ranking.dto.MyRankingResponse
import com.runnershi.domain.ranking.dto.RankingListResponse
import com.runnershi.domain.ranking.service.RankingService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Ranking", description = "랭킹 API")
@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val rankingService: RankingService
) {

    @Operation(summary = "지역별 랭킹 조회", description = "내 지역의 러너 랭킹을 커서 기반 페이징 조회")
    @GetMapping
    fun getRankings(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<RankingListResponse> {
        val response = rankingService.getRankings(userId, cursor, size)
        return ApiResponse.success(response)
    }

    @Operation(summary = "내 순위 조회", description = "내 지역에서의 내 순위 조회")
    @GetMapping("/me")
    fun getMyRanking(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<MyRankingResponse> {
        val response = rankingService.getMyRanking(userId)
        return ApiResponse.success(response)
    }
}
