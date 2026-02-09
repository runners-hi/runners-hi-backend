package com.runnershi.domain.running.dto

import com.runnershi.domain.running.entity.RunningRecord
import java.time.LocalDate
import java.time.LocalDateTime

data class RunningRecordResponse(
    val id: Long,
    val runningDate: LocalDate,
    val distance: Int,
    val duration: Int,
    val pace: Int,
    val calories: Int?,
    val startedAt: LocalDateTime,
    val endedAt: LocalDateTime,
    val memo: String?
) {
    companion object {
        fun from(record: RunningRecord) = RunningRecordResponse(
            id = record.id,
            runningDate = record.runningDate,
            distance = record.distance,
            duration = record.duration,
            pace = record.pace,
            calories = record.calories,
            startedAt = record.startedAt,
            endedAt = record.endedAt,
            memo = record.memo
        )
    }
}

data class RunningRecordListResponse(
    val records: List<RunningRecordResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

// === 일일 기록 집계 ===
// - 별도 summary 테이블 없이 running_records에서 실시간 집계
// - 해당 날짜의 모든 개별 러닝 기록을 조회 후 서비스 레이어에서 합산
// - totalDistance: 해당일 전체 러닝 거리 합산 (m)
// - totalDuration: 해당일 전체 러닝 시간 합산 (초)
// - averagePace: totalDuration / (totalDistance / 1000.0) → 초/km, 거리 0이면 0
// - runCount: 해당일 러닝 횟수 (1회 러닝 시작~종료 = 1건)
data class DailyRecordResponse(
    val date: LocalDate,
    val totalDistance: Int,
    val totalDuration: Int,
    val averagePace: Int,
    val runCount: Int,
    val records: List<RunningRecordResponse>
)
