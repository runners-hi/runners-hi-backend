package com.runnershi.domain.running.repository

import com.runnershi.domain.running.entity.RunningRecord
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.repository.UserRepository
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunningRecordRepositoryTest : RepositoryTest() {

    @Autowired lateinit var runningRecordRepository: RunningRecordRepository
    @Autowired lateinit var userRepository: UserRepository

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = userRepository.save(User(provider = Provider.KAKAO, providerId = "kakao-123", nickname = "테스트러너"))
    }

    private fun createRecord(
        date: LocalDate = LocalDate.of(2025, 1, 6),
        distance: Int = 5000,
        duration: Int = 1800
    ): RunningRecord {
        return runningRecordRepository.save(RunningRecord(
            userId = user.id,
            runningDate = date,
            distance = distance,
            duration = duration,
            pace = (duration / (distance / 1000.0)).toInt(),
            startedAt = date.atTime(7, 0),
            endedAt = date.atTime(7, 30)
        ))
    }

    @Nested
    @DisplayName("findByUserIdWithCursor")
    inner class FindByUserIdWithCursor {

        @Test
        @DisplayName("커서 없이 최신순 조회")
        fun withoutCursor() {
            createRecord(date = LocalDate.of(2025, 1, 6))
            createRecord(date = LocalDate.of(2025, 1, 7))
            createRecord(date = LocalDate.of(2025, 1, 8))

            val result = runningRecordRepository.findByUserIdWithCursor(user.id, null, PageRequest.of(0, 10))

            assertEquals(3, result.size)
            assertTrue(result[0].id > result[1].id) // ID 내림차순
        }

        @Test
        @DisplayName("커서 기반 다음 페이지 조회")
        fun withCursor() {
            val r1 = createRecord(date = LocalDate.of(2025, 1, 6))
            val r2 = createRecord(date = LocalDate.of(2025, 1, 7))
            val r3 = createRecord(date = LocalDate.of(2025, 1, 8))

            val result = runningRecordRepository.findByUserIdWithCursor(user.id, r3.id, PageRequest.of(0, 10))

            assertEquals(2, result.size)
            assertEquals(r2.id, result[0].id)
            assertEquals(r1.id, result[1].id)
        }
    }

    @Nested
    @DisplayName("findByUserIdAndRunningDate")
    inner class FindByUserIdAndRunningDate {

        @Test
        @DisplayName("특정 날짜 기록 조회")
        fun success() {
            val date = LocalDate.of(2025, 1, 6)
            createRecord(date = date)
            createRecord(date = date)
            createRecord(date = LocalDate.of(2025, 1, 7)) // 다른 날짜

            val result = runningRecordRepository.findByUserIdAndRunningDate(user.id, date)

            assertEquals(2, result.size)
        }
    }

    @Nested
    @DisplayName("findByUserIdAndDateRange")
    inner class FindByUserIdAndDateRange {

        @Test
        @DisplayName("날짜 범위 내 기록 조회")
        fun success() {
            createRecord(date = LocalDate.of(2025, 1, 5)) // 범위 밖
            createRecord(date = LocalDate.of(2025, 1, 6))
            createRecord(date = LocalDate.of(2025, 1, 8))
            createRecord(date = LocalDate.of(2025, 1, 12))
            createRecord(date = LocalDate.of(2025, 1, 13)) // 범위 밖

            val result = runningRecordRepository.findByUserIdAndDateRange(
                user.id,
                LocalDate.of(2025, 1, 6),
                LocalDate.of(2025, 1, 12)
            )

            assertEquals(3, result.size)
        }
    }

    @Nested
    @DisplayName("집계 쿼리")
    inner class AggregationQueries {

        @BeforeEach
        fun setUpRecords() {
            createRecord(date = LocalDate.of(2025, 1, 6), distance = 3000, duration = 1200)
            createRecord(date = LocalDate.of(2025, 1, 6), distance = 5000, duration = 1800)
            createRecord(date = LocalDate.of(2025, 1, 7), distance = 4000, duration = 1500)
        }

        @Test
        @DisplayName("전체 거리 합산")
        fun sumDistanceByUserId() {
            val total = runningRecordRepository.sumDistanceByUserId(user.id)
            assertEquals(12000L, total)
        }

        @Test
        @DisplayName("기간 내 거리 합산")
        fun sumDistanceByDateRange() {
            val total = runningRecordRepository.sumDistanceByUserIdAndDateRange(
                user.id, LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 6)
            )
            assertEquals(8000L, total)
        }

        @Test
        @DisplayName("기간 내 시간 합산")
        fun sumDurationByDateRange() {
            val total = runningRecordRepository.sumDurationByUserIdAndDateRange(
                user.id, LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 7)
            )
            assertEquals(4500, total)
        }

        @Test
        @DisplayName("기간 내 러닝 일수 카운트")
        fun countRunningDays() {
            val days = runningRecordRepository.countRunningDaysByUserIdAndDateRange(
                user.id, LocalDate.of(2025, 1, 6), LocalDate.of(2025, 1, 7)
            )
            assertEquals(2, days) // 1/6, 1/7 (1/6에 2회 뛰어도 1일)
        }
    }
}
