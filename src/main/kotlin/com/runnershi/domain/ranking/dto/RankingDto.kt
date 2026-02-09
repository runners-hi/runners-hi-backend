package com.runnershi.domain.ranking.dto

import com.runnershi.domain.user.entity.Tier

// === 랭킹 API 응답 ===
// - 순위 기준: totalDistance DESC (같은 지역 내)
// - 동점 처리: 같은 totalDistance면 같은 순위, 다음 순위는 건너뜀 (RANK 방식: 1,1,3)
// - 동순위 내 정렬: 가입순 (id ASC)
// - 대상: ACTIVE 유저 중 지역 선택한 유저만
// - 거리 단위: m (프론트에서 km 변환)

data class RankingResponse(
    val rank: Int,
    val userId: Long,
    val nickname: String,
    val profileImageUrl: String?,
    val tier: Tier,
    val totalDistance: Int
)

data class RankingListResponse(
    val rankings: List<RankingResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

data class MyRankingResponse(
    val rank: Int,
    val nickname: String,
    val profileImageUrl: String?,
    val tier: Tier,
    val totalDistance: Int
)
