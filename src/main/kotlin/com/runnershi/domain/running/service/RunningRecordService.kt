package com.runnershi.domain.running.service

import com.runnershi.domain.running.dto.DailyRecordResponse
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.repository.RunningRecordRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class RunningRecordService(
    private val runningRecordRepository: RunningRecordRepository
) {

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
