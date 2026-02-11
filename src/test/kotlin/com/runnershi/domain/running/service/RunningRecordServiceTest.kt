package com.runnershi.domain.running.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.level.service.LevelService
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.running.dto.RunningRecordCreateRequest
import com.runnershi.domain.running.entity.RunningRecord
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.data.domain.PageRequest
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RunningRecordServiceTest {

    @Mock lateinit var runningRecordRepository: RunningRecordRepository
    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var levelService: LevelService
    @Mock lateinit var missionChecker: MissionChecker

    private lateinit var service: RunningRecordService

    @BeforeEach
    fun setUp() {
        service = RunningRecordService(runningRecordRepository, userRepository, levelService, missionChecker)
    }

    private fun createUser(id: Long = 1L, totalDistance: Int = 0): User {
        val user = User(provider = Provider.KAKAO, providerId = "kakao-123", nickname = "테스트러너")
        user.totalDistance = totalDistance
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    private fun createRecord(id: Long = 1L, userId: Long = 1L, distance: Int = 5000, duration: Int = 1800, date: LocalDate = LocalDate.of(2025, 1, 6)): RunningRecord {
        val record = RunningRecord(
            userId = userId,
            runningDate = date,
            distance = distance,
            duration = duration,
            pace = (duration / (distance / 1000.0)).toInt(),
            startedAt = date.atTime(7, 0),
            endedAt = date.atTime(7, 30)
        )
        ReflectionTestUtils.setField(record, "id", id)
        return record
    }

    private fun createRequest(distance: Int = 5000, duration: Int = 1800): RunningRecordCreateRequest {
        val now = LocalDateTime.of(2025, 1, 6, 7, 0)
        return RunningRecordCreateRequest(
            distance = distance,
            duration = duration,
            calories = 300,
            startedAt = now,
            endedAt = now.plusMinutes(30),
            memo = "테스트 러닝"
        )
    }

    @Nested
    @DisplayName("러닝 기록 저장")
    inner class CreateRunningRecord {

        @Test
        @DisplayName("기록 저장 성공 - pace 계산, totalDistance 갱신, 미션 체크")
        fun success() {
            val user = createUser(totalDistance = 10000)
            val request = createRequest(distance = 5000, duration = 1800)
            val savedRecord = createRecord()

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(runningRecordRepository.save(any<RunningRecord>())).thenReturn(savedRecord)

            val response = service.createRunningRecord(1L, request)

            assertEquals(15000, user.totalDistance) // 10000 + 5000
            verify(missionChecker).checkOnRunningRecordCreated(any(), any())
            assertEquals(savedRecord.id, response.id)
        }

        @Test
        @DisplayName("pace 계산 - duration(초) / distance(km)")
        fun paceCalculation() {
            val user = createUser()
            val request = createRequest(distance = 5000, duration = 1500) // 5km, 25분 → 300초/km
            val expectedPace = (1500 / (5000 / 1000.0)).toInt() // 300

            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(runningRecordRepository.save(any<RunningRecord>())).thenAnswer { it.arguments[0] }

            val response = service.createRunningRecord(1L, request)

            assertEquals(expectedPace, response.pace)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            assertThrows<BusinessException> {
                service.createRunningRecord(999L, createRequest())
            }.also { assertEquals(ErrorCode.USER_NOT_FOUND, it.errorCode) }
        }
    }

    @Nested
    @DisplayName("주간 요약 조회")
    inner class GetWeeklySummary {

        @Test
        @DisplayName("주간 요약 조회 성공 - 월요일~일요일 기준")
        fun success() {
            val date = LocalDate.of(2025, 1, 8) // 수요일
            val monday = LocalDate.of(2025, 1, 6)
            val sunday = LocalDate.of(2025, 1, 12)

            whenever(runningRecordRepository.sumDistanceByUserIdAndDateRange(1L, monday, sunday)).thenReturn(10000L)
            whenever(runningRecordRepository.sumDurationByUserIdAndDateRange(1L, monday, sunday)).thenReturn(3600)
            whenever(runningRecordRepository.countRunningDaysByUserIdAndDateRange(1L, monday, sunday)).thenReturn(3)
            whenever(runningRecordRepository.findByUserIdAndDateRange(1L, monday, sunday)).thenReturn(listOf(createRecord(), createRecord(id = 2L), createRecord(id = 3L)))

            val response = service.getWeeklySummary(1L, date)

            assertEquals(monday, response.startDate)
            assertEquals(sunday, response.endDate)
            assertEquals(10000L, response.totalDistance)
            assertEquals(3, response.runDays)
            assertEquals(360, response.averagePace) // 3600 / 10km
        }

        @Test
        @DisplayName("기록이 없는 주 - 0 반환")
        fun noRecords() {
            val monday = LocalDate.of(2025, 1, 6)
            val sunday = LocalDate.of(2025, 1, 12)

            whenever(runningRecordRepository.sumDistanceByUserIdAndDateRange(1L, monday, sunday)).thenReturn(0L)
            whenever(runningRecordRepository.sumDurationByUserIdAndDateRange(1L, monday, sunday)).thenReturn(0)
            whenever(runningRecordRepository.countRunningDaysByUserIdAndDateRange(1L, monday, sunday)).thenReturn(0)
            whenever(runningRecordRepository.findByUserIdAndDateRange(1L, monday, sunday)).thenReturn(emptyList())

            val response = service.getWeeklySummary(1L, monday)

            assertEquals(0L, response.totalDistance)
            assertEquals(0, response.averagePace)
            assertEquals(0, response.runDays)
        }
    }

    @Nested
    @DisplayName("일일 기록 조회")
    inner class GetDailyRecord {

        @Test
        @DisplayName("일일 기록 합산 조회 성공")
        fun success() {
            val date = LocalDate.of(2025, 1, 6)
            val records = listOf(
                createRecord(id = 1L, distance = 3000, duration = 1200, date = date),
                createRecord(id = 2L, distance = 5000, duration = 1800, date = date)
            )
            whenever(runningRecordRepository.findByUserIdAndRunningDate(1L, date)).thenReturn(records)

            val response = service.getDailyRecord(1L, date)

            assertEquals(8000, response.totalDistance)
            assertEquals(3000, response.totalDuration)
            assertEquals(2, response.runCount)
        }

        @Test
        @DisplayName("기록이 없는 날 - 0 반환")
        fun noRecords() {
            val date = LocalDate.of(2025, 1, 6)
            whenever(runningRecordRepository.findByUserIdAndRunningDate(1L, date)).thenReturn(emptyList())

            val response = service.getDailyRecord(1L, date)

            assertEquals(0, response.totalDistance)
            assertEquals(0, response.averagePace)
            assertEquals(0, response.runCount)
        }
    }

    @Nested
    @DisplayName("기록 목록 조회")
    inner class GetRunningRecords {

        @Test
        @DisplayName("커서 기반 페이지네이션 - 다음 페이지 있음")
        fun hasNext() {
            val records = (1..3).map { createRecord(id = it.toLong()) }
            whenever(runningRecordRepository.findByUserIdWithCursor(any(), anyOrNull(), any())).thenReturn(records)

            val response = service.getRunningRecords(1L, null, 2)

            assertEquals(2, response.records.size)
            assertTrue(response.hasNext)
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 - 마지막 페이지")
        fun lastPage() {
            val records = listOf(createRecord(id = 1L))
            whenever(runningRecordRepository.findByUserIdWithCursor(any(), anyOrNull(), any())).thenReturn(records)

            val response = service.getRunningRecords(1L, null, 2)

            assertEquals(1, response.records.size)
            assertFalse(response.hasNext)
        }
    }
}
