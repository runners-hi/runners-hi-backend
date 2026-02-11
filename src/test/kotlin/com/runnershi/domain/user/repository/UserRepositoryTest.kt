package com.runnershi.domain.user.repository

import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest : RepositoryTest() {

    @Autowired
    lateinit var userRepository: UserRepository

    private fun createUser(
        provider: Provider = Provider.KAKAO,
        providerId: String,
        nickname: String,
        regionId: Long? = null,
        totalDistance: Int = 0,
        status: UserStatus = UserStatus.ACTIVE
    ): User {
        val user = User(
            provider = provider,
            providerId = providerId,
            nickname = nickname,
            status = status
        )
        user.regionId = regionId
        user.totalDistance = totalDistance
        return userRepository.save(user)
    }

    @Nested
    @DisplayName("findByProviderAndProviderId")
    inner class FindByProviderAndProviderId {

        @Test
        @DisplayName("일치하는 유저가 있으면 반환")
        fun found() {
            createUser(providerId = "kakao-123", nickname = "유저1")

            val result = userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao-123")

            assertNotNull(result)
            assertEquals("유저1", result.nickname)
        }

        @Test
        @DisplayName("일치하는 유저가 없으면 null 반환")
        fun notFound() {
            val result = userRepository.findByProviderAndProviderId(Provider.KAKAO, "nonexistent")

            assertNull(result)
        }

        @Test
        @DisplayName("다른 Provider는 매칭되지 않음")
        fun differentProvider() {
            createUser(provider = Provider.KAKAO, providerId = "id-123", nickname = "유저1")

            val result = userRepository.findByProviderAndProviderId(Provider.GOOGLE, "id-123")

            assertNull(result)
        }
    }

    @Nested
    @DisplayName("existsByNickname")
    inner class ExistsByNickname {

        @Test
        @DisplayName("닉네임이 존재하면 true")
        fun exists() {
            createUser(providerId = "kakao-1", nickname = "존재하는닉네임")

            assertTrue(userRepository.existsByNickname("존재하는닉네임"))
        }

        @Test
        @DisplayName("닉네임이 없으면 false")
        fun notExists() {
            assertFalse(userRepository.existsByNickname("없는닉네임"))
        }
    }

    @Nested
    @DisplayName("findRanking")
    inner class FindRanking {

        @Test
        @DisplayName("같은 지역의 ACTIVE 유저를 거리 내림차순으로 조회")
        fun rankedByDistance() {
            createUser(providerId = "u1", nickname = "유저1", regionId = 1L, totalDistance = 5000, status = UserStatus.ACTIVE)
            createUser(providerId = "u2", nickname = "유저2", regionId = 1L, totalDistance = 10000, status = UserStatus.ACTIVE)
            createUser(providerId = "u3", nickname = "유저3", regionId = 1L, totalDistance = 3000, status = UserStatus.ACTIVE)
            // 다른 지역 유저 - 제외되어야 함
            createUser(providerId = "u4", nickname = "유저4", regionId = 2L, totalDistance = 20000, status = UserStatus.ACTIVE)
            // WITHDRAWN 유저 - 제외되어야 함
            createUser(providerId = "u5", nickname = "유저5", regionId = 1L, totalDistance = 15000, status = UserStatus.WITHDRAWN)

            val result = userRepository.findRanking(1L, UserStatus.ACTIVE, PageRequest.of(0, 10))

            assertEquals(3, result.size)
            assertEquals("유저2", result[0].nickname) // 10000m
            assertEquals("유저1", result[1].nickname) // 5000m
            assertEquals("유저3", result[2].nickname) // 3000m
        }
    }

    @Nested
    @DisplayName("findRankingWithCursor")
    inner class FindRankingWithCursor {

        @Test
        @DisplayName("커서 기반 페이지네이션으로 다음 페이지 조회")
        fun cursorPagination() {
            val u1 = createUser(providerId = "u1", nickname = "유저1", regionId = 1L, totalDistance = 10000)
            val u2 = createUser(providerId = "u2", nickname = "유저2", regionId = 1L, totalDistance = 5000)
            val u3 = createUser(providerId = "u3", nickname = "유저3", regionId = 1L, totalDistance = 3000)

            // u1(10000m) 이후 커서
            val result = userRepository.findRankingWithCursor(
                regionId = 1L,
                status = UserStatus.ACTIVE,
                cursorDistance = 10000,
                cursorId = u1.id,
                pageable = PageRequest.of(0, 10)
            )

            assertEquals(2, result.size)
            assertEquals("유저2", result[0].nickname) // 5000m
            assertEquals("유저3", result[1].nickname) // 3000m
        }
    }

    @Nested
    @DisplayName("countRank")
    inner class CountRank {

        @Test
        @DisplayName("나보다 거리가 높은 유저 수 + 1 = 내 순위")
        fun calculateRank() {
            createUser(providerId = "u1", nickname = "유저1", regionId = 1L, totalDistance = 10000)
            createUser(providerId = "u2", nickname = "유저2", regionId = 1L, totalDistance = 5000)
            createUser(providerId = "u3", nickname = "유저3", regionId = 1L, totalDistance = 3000)

            // 5000m 유저의 순위 = 10000m보다 높은 사람 1명 + 1 = 2등
            val rank = userRepository.countRank(1L, UserStatus.ACTIVE, 5000)
            assertEquals(2L, rank)
        }

        @Test
        @DisplayName("동점자는 같은 순위")
        fun tiedRank() {
            createUser(providerId = "u1", nickname = "유저1", regionId = 1L, totalDistance = 10000)
            createUser(providerId = "u2", nickname = "유저2", regionId = 1L, totalDistance = 10000)
            createUser(providerId = "u3", nickname = "유저3", regionId = 1L, totalDistance = 5000)

            // 10000m 유저의 순위 = 자신보다 높은 사람 0명 + 1 = 1등
            val rank = userRepository.countRank(1L, UserStatus.ACTIVE, 10000)
            assertEquals(1L, rank)
        }
    }
}
