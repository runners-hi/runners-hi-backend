package com.runnershi.domain.level.service

import com.runnershi.domain.level.entity.UserLevelSnapshot
import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.Tier
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.util.ReflectionTestUtils
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserLevelSnapshotServiceTest {

    @Mock lateinit var userLevelSnapshotRepository: UserLevelSnapshotRepository

    private lateinit var userLevelSnapshotService: UserLevelSnapshotService

    @BeforeEach
    fun setUp() {
        userLevelSnapshotService = UserLevelSnapshotService(userLevelSnapshotRepository)
        nextUserId = 1L
    }

    private var nextUserId = 1L

    private fun createUser(nickname: String, level: Int = 5, tier: Tier = Tier.SILVER, experience: Int = 300, totalDistance: Int = 10000): User {
        val user = User(
            provider = Provider.KAKAO,
            providerId = "provider-$nickname",
            nickname = nickname,
            tier = tier,
            level = level,
            experience = experience,
            totalDistance = totalDistance,
            status = UserStatus.ACTIVE
        )
        ReflectionTestUtils.setField(user, "id", nextUserId++)
        return user
    }

    private fun createSnapshot(userId: Long, year: Int, month: Int): UserLevelSnapshot {
        return UserLevelSnapshot(
            userId = userId,
            year = year,
            month = month,
            level = 5,
            tier = Tier.SILVER,
            experience = 300,
            totalDistance = 10000
        )
    }

    @Nested
    @DisplayName("saveYearEndSnapshots")
    inner class SaveYearEndSnapshots {

        @Test
        @DisplayName("스냅샷 저장 성공 - year/month/level/tier/experience/totalDistance 저장")
        fun savesSnapshotsForAllUsers() {
            val user1 = createUser("user1", level = 5, tier = Tier.SILVER, experience = 300, totalDistance = 10000)
            val user2 = createUser("user2", level = 10, tier = Tier.GOLD, experience = 800, totalDistance = 50000)
            val year = 2024

            whenever(userLevelSnapshotRepository.findFirstByUserIdAndYearOrderByMonthDesc(any(), any()))
                .thenReturn(null)

            userLevelSnapshotService.saveYearEndSnapshots(listOf(user1, user2), year)

            val captor = argumentCaptor<List<UserLevelSnapshot>>()
            verify(userLevelSnapshotRepository).saveAll(captor.capture())

            val saved = captor.firstValue
            assertEquals(2, saved.size)

            val snap1 = saved.find { it.userId == user1.id }!!
            assertEquals(year, snap1.year)
            assertEquals(12, snap1.month)
            assertEquals(user1.level, snap1.level)
            assertEquals(user1.tier, snap1.tier)
            assertEquals(user1.experience, snap1.experience)
            assertEquals(user1.totalDistance, snap1.totalDistance)

            val snap2 = saved.find { it.userId == user2.id }!!
            assertEquals(year, snap2.year)
            assertEquals(12, snap2.month)
            assertEquals(user2.level, snap2.level)
            assertEquals(user2.tier, snap2.tier)
            assertEquals(user2.experience, snap2.experience)
            assertEquals(user2.totalDistance, snap2.totalDistance)
        }

        @Test
        @DisplayName("이미 12월 스냅샷 있으면 스킵")
        fun skipsUsersWithExistingDecemberSnapshot() {
            val user1 = createUser("user1")
            val user2 = createUser("user2")
            val year = 2024

            whenever(userLevelSnapshotRepository.findFirstByUserIdAndYearOrderByMonthDesc(any(), any()))
                .thenReturn(createSnapshot(0L, year, month = 12))

            userLevelSnapshotService.saveYearEndSnapshots(listOf(user1, user2), year)

            verify(userLevelSnapshotRepository, never()).saveAll(any<List<UserLevelSnapshot>>())
        }

        @Test
        @DisplayName("일부만 스킵 - 이미 있는 유저는 제외하고 없는 유저만 저장")
        fun savesOnlyUsersWithoutExistingSnapshot() {
            val user1 = createUser("user1")
            val user2 = createUser("user2")
            val year = 2024

            // user1은 이미 12월 스냅샷 존재, user2는 없음
            whenever(userLevelSnapshotRepository.findFirstByUserIdAndYearOrderByMonthDesc(user1.id, year))
                .thenReturn(createSnapshot(user1.id, year, month = 12))
            whenever(userLevelSnapshotRepository.findFirstByUserIdAndYearOrderByMonthDesc(user2.id, year))
                .thenReturn(null)

            userLevelSnapshotService.saveYearEndSnapshots(listOf(user1, user2), year)

            val captor = argumentCaptor<List<UserLevelSnapshot>>()
            verify(userLevelSnapshotRepository).saveAll(captor.capture())

            val saved = captor.firstValue
            assertEquals(1, saved.size)
            assertEquals(user2.id, saved[0].userId)
        }
    }
}
