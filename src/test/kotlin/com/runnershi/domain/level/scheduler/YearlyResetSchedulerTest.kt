package com.runnershi.domain.level.scheduler

import com.runnershi.domain.level.service.UserLevelSnapshotService
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.Tier
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YearlyResetSchedulerTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var userLevelSnapshotService: UserLevelSnapshotService

    private lateinit var yearlyResetScheduler: YearlyResetScheduler

    @BeforeEach
    fun setUp() {
        yearlyResetScheduler = YearlyResetScheduler(userRepository, userLevelSnapshotService)
    }

    private fun createActiveUser(nickname: String, experience: Int): User {
        return User(
            provider = Provider.KAKAO,
            providerId = "provider-$nickname",
            nickname = nickname,
            tier = Tier.BRONZE,
            level = 1,
            experience = experience,
            status = UserStatus.ACTIVE
        )
    }

    @Nested
    @DisplayName("resetYearlyExperience")
    inner class ResetYearlyExperience {

        @Test
        @DisplayName("ACTIVE 유저의 경험치를 0으로 초기화")
        fun resetsExperienceForActiveUsers() {
            val user1 = createActiveUser("user1", 500)
            val user2 = createActiveUser("user2", 300)
            whenever(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(listOf(user1, user2))

            yearlyResetScheduler.resetYearlyExperience()

            assertEquals(0, user1.experience)
            assertEquals(0, user2.experience)
        }

        @Test
        @DisplayName("스냅샷을 초기화 전에 저장")
        fun savesSnapshotsBeforeResettingExperience() {
            val user1 = createActiveUser("user1", 500)
            val user2 = createActiveUser("user2", 300)
            val activeUsers = listOf(user1, user2)
            whenever(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(activeUsers)

            val snapshotCallOrder = mutableListOf<String>()
            org.mockito.kotlin.doAnswer {
                snapshotCallOrder.add("saveSnapshots")
                Unit
            }.whenever(userLevelSnapshotService).saveYearEndSnapshots(any(), any())

            yearlyResetScheduler.resetYearlyExperience()

            verify(userLevelSnapshotService).saveYearEndSnapshots(any(), any())
            // 스냅샷 저장 후 경험치가 0으로 리셋되었음을 확인
            assertEquals(0, user1.experience)
            assertEquals(0, user2.experience)
        }

        @Test
        @DisplayName("ACTIVE 유저 없으면 아무 작업 없음")
        fun doesNothingWhenNoActiveUsers() {
            whenever(userRepository.findAllByStatus(UserStatus.ACTIVE))
                .thenReturn(emptyList())

            yearlyResetScheduler.resetYearlyExperience()

            verify(userLevelSnapshotService, never()).saveYearEndSnapshots(any(), any())
        }
    }
}
