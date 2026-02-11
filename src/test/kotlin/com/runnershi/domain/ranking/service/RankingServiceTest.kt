package com.runnershi.domain.ranking.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.Tier
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RankingServiceTest {

    @Mock lateinit var userRepository: UserRepository

    private lateinit var rankingService: RankingService

    @BeforeEach
    fun setUp() {
        rankingService = RankingService(userRepository)
    }

    private fun createUser(id: Long, nickname: String, regionId: Long? = 1L, totalDistance: Int = 0): User {
        val user = User(provider = Provider.KAKAO, providerId = "kakao-$id", nickname = nickname, status = UserStatus.ACTIVE)
        user.regionId = regionId
        user.totalDistance = totalDistance
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    @Nested
    @DisplayName("지역별 랭킹 조회")
    inner class GetRankings {

        @Test
        @DisplayName("랭킹 조회 성공 - 거리 내림차순, 순위 계산")
        fun success() {
            val me = createUser(1L, "나", regionId = 1L, totalDistance = 5000)
            val rankedUsers = listOf(
                createUser(2L, "1등", totalDistance = 10000),
                createUser(3L, "2등", totalDistance = 5000),
                createUser(1L, "나", totalDistance = 5000)
            )

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(me))
            whenever(userRepository.findRanking(eq(1L), eq(UserStatus.ACTIVE), any())).thenReturn(rankedUsers)
            whenever(userRepository.countRank(1L, UserStatus.ACTIVE, 10000)).thenReturn(1L)
            whenever(userRepository.countRank(1L, UserStatus.ACTIVE, 5000)).thenReturn(2L)

            val response = rankingService.getRankings(1L, null, 20)

            assertEquals(3, response.rankings.size)
            assertEquals(1, response.rankings[0].rank) // 10000m → 1등
            assertEquals(2, response.rankings[1].rank) // 5000m → 2등 (동점)
            assertEquals(2, response.rankings[2].rank) // 5000m → 2등 (동점)
            assertFalse(response.hasNext)
        }

        @Test
        @DisplayName("지역 미선택 시 예외 발생")
        fun regionNotSelected() {
            val user = createUser(1L, "나", regionId = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            val exception = assertThrows<BusinessException> {
                rankingService.getRankings(1L, null, 20)
            }
            assertEquals(ErrorCode.REGION_NOT_SELECTED, exception.errorCode)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows<BusinessException> {
                rankingService.getRankings(999L, null, 20)
            }.also { assertEquals(ErrorCode.USER_NOT_FOUND, it.errorCode) }
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 - 다음 페이지 있음")
        fun withCursor_hasNext() {
            val me = createUser(1L, "나", regionId = 1L)
            val cursorUser = createUser(10L, "커서유저", totalDistance = 8000)
            val nextUsers = (1..3).map { createUser(it.toLong() + 10, "유저$it", totalDistance = 5000 - it * 1000) }

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(me))
            whenever(userRepository.findById(10L)).thenReturn(Optional.of(cursorUser))
            whenever(userRepository.findRankingWithCursor(eq(1L), eq(UserStatus.ACTIVE), eq(8000), eq(10L), any())).thenReturn(nextUsers)
            whenever(userRepository.countRank(any(), any(), any())).thenReturn(3L)

            val response = rankingService.getRankings(1L, 10L, 2)

            assertEquals(2, response.rankings.size)
            assertTrue(response.hasNext)
        }
    }

    @Nested
    @DisplayName("내 순위 조회")
    inner class GetMyRanking {

        @Test
        @DisplayName("내 순위 조회 성공")
        fun success() {
            val me = createUser(1L, "나", regionId = 1L, totalDistance = 5000)
            me.tier = Tier.SILVER

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(me))
            whenever(userRepository.countRank(1L, UserStatus.ACTIVE, 5000)).thenReturn(3L)

            val response = rankingService.getMyRanking(1L)

            assertEquals(3, response.rank)
            assertEquals("나", response.nickname)
            assertEquals(Tier.SILVER, response.tier)
            assertEquals(5000, response.totalDistance)
        }

        @Test
        @DisplayName("지역 미선택 시 예외 발생")
        fun regionNotSelected() {
            val user = createUser(1L, "나", regionId = null)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

            assertThrows<BusinessException> {
                rankingService.getMyRanking(1L)
            }.also { assertEquals(ErrorCode.REGION_NOT_SELECTED, it.errorCode) }
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows<BusinessException> {
                rankingService.getMyRanking(999L)
            }.also { assertEquals(ErrorCode.USER_NOT_FOUND, it.errorCode) }
        }
    }
}
