package com.runnershi.domain.user.scheduler

import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.user.entity.Provider
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
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WithdrawnUserCleanupSchedulerTest {

    @Mock
    lateinit var userRepository: UserRepository

    @Mock
    lateinit var runningRecordRepository: RunningRecordRepository

    @Mock
    lateinit var userMissionRepository: UserMissionRepository

    @Mock
    lateinit var userLevelSnapshotRepository: UserLevelSnapshotRepository

    @Mock
    lateinit var termsAgreementRepository: TermsAgreementRepository

    private lateinit var scheduler: WithdrawnUserCleanupScheduler

    @BeforeEach
    fun setUp() {
        scheduler = WithdrawnUserCleanupScheduler(
            userRepository,
            runningRecordRepository,
            userMissionRepository,
            userLevelSnapshotRepository,
            termsAgreementRepository
        )
    }

    private fun createWithdrawnUser(
        id: Long = 1L,
        deletedAt: LocalDateTime = LocalDateTime.now().minusDays(31)
    ): User {
        val user = User(
            provider = Provider.KAKAO,
            providerId = "kakao-$id",
            email = "user$id@test.com",
            nickname = "탈퇴예정유저$id",
            profileImageUrl = "https://example.com/profile$id.jpg",
            fcmToken = "fcm-token-$id",
            status = UserStatus.WITHDRAWN
        )
        user.deletedAt = deletedAt
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    @Nested
    @DisplayName("30일 경과 유저 정리")
    inner class CleanupExpiredUsers {

        @Test
        @DisplayName("30일 경과 유저의 개인정보가 마스킹된다")
        fun masksPersonalInfo() {
            val user = createWithdrawnUser(id = 1L)
            whenever(userRepository.findByStatusAndDeletedAtBefore(eq(UserStatus.WITHDRAWN), any()))
                .thenReturn(listOf(user))

            scheduler.cleanupWithdrawnUsers()

            assertTrue(user.nickname.startsWith("탈퇴한사용자_"))
            assertNull(user.email)
            assertNull(user.profileImageUrl)
            assertNull(user.fcmToken)
            assertTrue(user.providerId.startsWith("deleted_"))
            assertEquals(UserStatus.DELETED, user.status)
        }

        @Test
        @DisplayName("30일 경과 유저의 연관 데이터가 삭제된다")
        fun deletesRelatedData() {
            val user = createWithdrawnUser(id = 1L)
            whenever(userRepository.findByStatusAndDeletedAtBefore(eq(UserStatus.WITHDRAWN), any()))
                .thenReturn(listOf(user))

            scheduler.cleanupWithdrawnUsers()

            verify(runningRecordRepository).deleteAllByUserId(1L)
            verify(userMissionRepository).deleteAllByUserId(1L)
            verify(userLevelSnapshotRepository).deleteAllByUserId(1L)
            verify(termsAgreementRepository).deleteAllByUserId(1L)
        }

        @Test
        @DisplayName("여러 유저가 동시에 정리된다")
        fun cleansUpMultipleUsers() {
            val user1 = createWithdrawnUser(id = 1L)
            val user2 = createWithdrawnUser(id = 2L)
            whenever(userRepository.findByStatusAndDeletedAtBefore(eq(UserStatus.WITHDRAWN), any()))
                .thenReturn(listOf(user1, user2))

            scheduler.cleanupWithdrawnUsers()

            verify(runningRecordRepository).deleteAllByUserId(1L)
            verify(runningRecordRepository).deleteAllByUserId(2L)
            assertEquals(UserStatus.DELETED, user1.status)
            assertEquals(UserStatus.DELETED, user2.status)
        }
    }

    @Nested
    @DisplayName("30일 미경과 유저 보호")
    inner class ProtectRecentUsers {

        @Test
        @DisplayName("정리 대상이 없으면 아무 작업도 하지 않는다")
        fun noUsersToCleanup() {
            whenever(userRepository.findByStatusAndDeletedAtBefore(eq(UserStatus.WITHDRAWN), any()))
                .thenReturn(emptyList())

            scheduler.cleanupWithdrawnUsers()

            verify(runningRecordRepository, never()).deleteAllByUserId(any())
            verify(userMissionRepository, never()).deleteAllByUserId(any())
            verify(userLevelSnapshotRepository, never()).deleteAllByUserId(any())
            verify(termsAgreementRepository, never()).deleteAllByUserId(any())
        }
    }

    @Nested
    @DisplayName("오류 처리")
    inner class ErrorHandling {

        @Test
        @DisplayName("한 유저 정리 실패해도 다른 유저 정리는 계속 진행")
        fun continuesOnFailure() {
            val user1 = createWithdrawnUser(id = 1L)
            val user2 = createWithdrawnUser(id = 2L)
            whenever(userRepository.findByStatusAndDeletedAtBefore(eq(UserStatus.WITHDRAWN), any()))
                .thenReturn(listOf(user1, user2))
            whenever(runningRecordRepository.deleteAllByUserId(1L))
                .thenThrow(RuntimeException("DB 오류"))

            scheduler.cleanupWithdrawnUsers()

            // user1 실패해도 user2는 정리됨
            verify(runningRecordRepository).deleteAllByUserId(2L)
            verify(userMissionRepository).deleteAllByUserId(2L)
            assertEquals(UserStatus.DELETED, user2.status)
        }
    }
}
