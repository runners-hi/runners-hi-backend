package com.runnershi.domain.running.service

import com.runnershi.domain.running.dto.DailyRecordResponse
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.dto.WeeklySummaryResponse
import com.runnershi.domain.running.repository.RunningRecordRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
class RunningRecordService(
    private val runningRecordRepository: RunningRecordRepository
) {

    // 주간 요약: DB 레벨 집계 쿼리로 합산 (별도 summary 테이블 없음)
    // - 주간 기준: 월요일 ~ 일요일 (ISO 8601)
    // - 글로벌 대응 시 getWeekStartDate() 로직만 변경하면 됨
    @Transactional(readOnly = true)
    fun getWeeklySummary(userId: Long, date: LocalDate): WeeklySummaryResponse {
        val startDate = getWeekStartDate(date)
        val endDate = startDate.plusDays(6)

        val totalDistance = runningRecordRepository.sumDistanceByUserIdAndDateRange(userId, startDate, endDate)
        val totalDuration = runningRecordRepository.sumDurationByUserIdAndDateRange(userId, startDate, endDate)
        val runDays = runningRecordRepository.countRunningDaysByUserIdAndDateRange(userId, startDate, endDate)
        val records = runningRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate)

        val averagePace = if (totalDistance > 0) {
            (totalDuration / (totalDistance / 1000.0)).toInt()
        } else 0

        return WeeklySummaryResponse(
            startDate = startDate,
            endDate = endDate,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averagePace = averagePace,
            runCount = records.size,
            runDays = runDays
        )
    }

    // 주간 시작일 계산 (월요일 기준)
    // 글로벌 대응 시 이 메서드만 locale 기반으로 변경
    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    // 일일 기록: 해당 날짜의 개별 러닝 기록을 조회 후 서비스에서 합산
    // - 별도 summary 테이블 없이 running_records 실시간 집계
    // - 하루 러닝 횟수가 현실적으로 1~3회이므로 성능 이슈 없음
    @Transactional(readOnly = true)
    fun getDailyRecord(userId: Long, date: LocalDate): DailyRecordResponse {
        val records = runningRecordRepository.findByUserIdAndRunningDate(userId, date)

        val totalDistance = records.sumOf { it.distance }
        val totalDuration = records.sumOf { it.duration }
        // 평균 페이스: 총 시간(초) / 총 거리(km) → 초/km
        val averagePace = if (totalDistance > 0) {
            (totalDuration / (totalDistance / 1000.0)).toInt()
        } else 0

        return DailyRecordResponse(
            date = date,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averagePace = averagePace,
            runCount = records.size,
            records = records.map { RunningRecordResponse.from(it) }
        )
    }

    @Transactional(readOnly = true)
    fun getRunningRecords(userId: Long, cursor: Long?, size: Int): RunningRecordListResponse {
        val pageable = PageRequest.of(0, size + 1)
        val records = runningRecordRepository.findByUserIdWithCursor(userId, cursor, pageable)

        val hasNext = records.size > size
        val content = if (hasNext) records.dropLast(1) else records

        return RunningRecordListResponse(
            records = content.map { RunningRecordResponse.from(it) },
            nextCursor = if (hasNext) content.last().id else null,
            hasNext = hasNext
        )
    }
}
