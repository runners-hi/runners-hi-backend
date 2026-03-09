package com.runnershi.domain.ranking.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.ranking.dto.MyRankingResponse
import com.runnershi.domain.ranking.dto.RankingListResponse
import com.runnershi.domain.ranking.dto.RankingResponse
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RankingService(
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getRankings(userId: Long, cursor: Long?, size: Int): RankingListResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val districtId = user.districtId
            ?: throw BusinessException(ErrorCode.REGION_NOT_SELECTED)

        val pageable = PageRequest.of(0, size + 1)

        val users = if (cursor != null) {
            val cursorUser = userRepository.findById(cursor).orElse(null)
            if (cursorUser != null) {
                userRepository.findRankingWithCursor(
                    districtId, UserStatus.ACTIVE,
                    cursorUser.totalDistance, cursorUser.id, pageable
                )
            } else {
                userRepository.findRanking(districtId, UserStatus.ACTIVE, pageable)
            }
        } else {
            userRepository.findRanking(districtId, UserStatus.ACTIVE, pageable)
        }

        val hasNext = users.size > size
        val content = if (hasNext) users.dropLast(1) else users

        // 순위 계산: 같은 totalDistance → 같은 순위 (RANK 방식)
        // distinct distance별로 한 번만 쿼리하여 N+1 방지
        val rankMap = content.map { it.totalDistance }.distinct().associateWith { distance ->
            userRepository.countRank(districtId, UserStatus.ACTIVE, distance).toInt()
        }

        val rankings = content.map { u ->
            RankingResponse(
                rank = rankMap[u.totalDistance]!!,
                userId = u.id,
                nickname = u.nickname,
                profileImageUrl = u.profileImageUrl,
                tier = u.tier,
                totalDistance = u.totalDistance
            )
        }

        return RankingListResponse(
            rankings = rankings,
            nextCursor = if (hasNext) content.last().id else null,
            hasNext = hasNext
        )
    }

    @Transactional(readOnly = true)
    fun getMyRanking(userId: Long): MyRankingResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }
        val districtId = user.districtId
            ?: throw BusinessException(ErrorCode.REGION_NOT_SELECTED)

        val rank = userRepository.countRank(districtId, UserStatus.ACTIVE, user.totalDistance).toInt()

        return MyRankingResponse(
            rank = rank,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            tier = user.tier,
            totalDistance = user.totalDistance
        )
    }
}
