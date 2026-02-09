package com.runnershi.domain.running.dto

import com.runnershi.domain.running.entity.RunningRecord
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

// === 러닝 기록 저장 ===
// - 클라이언트: distance, duration, calories, startedAt, endedAt, memo 전달
// - 서버 계산: pace (duration / distance(km) → 초/km), runningDate (startedAt 기준)
// - 저장 후: User.totalDistance 갱신, 경험치/레벨/티어 재계산
data class RunningRecordCreateRequest(
    @field:Min(value = 1, message = "거리는 1m 이상이어야 합니다")
    val distance: Int,

    @field:Min(value = 1, message = "시간은 1초 이상이어야 합니다")
    val duration: Int,

    val calories: Int? = null,

    @field:NotNull(message = "시작 시간은 필수입니다")
    val startedAt: LocalDateTime,

    @field:NotNull(message = "종료 시간은 필수입니다")
    val endedAt: LocalDateTime,

    val memo: String? = null
)

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

// === 주간 요약 집계 ===
// - 주간 기준: 월요일 ~ 일요일 (ISO 8601 표준, 한국 관행 동일)
// - 글로벌 대응 시 주간 시작일 계산 로직만 변경하면 됨
// - 별도 summary 테이블 없이 running_records DB 레벨 집계 쿼리 사용
// - totalDistance: 주간 전체 거리 합산 (m)
// - totalDuration: 주간 전체 시간 합산 (초)
// - averagePace: totalDuration / (totalDistance / 1000.0) → 초/km, 거리 0이면 0
// - runCount: 주간 전체 러닝 횟수
// - runDays: 주간 중 실제 러닝한 일수 (하루에 여러 번 뛰어도 1일로 카운트)
data class WeeklySummaryResponse(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val totalDistance: Long,
    val totalDuration: Int,
    val averagePace: Int,
    val runCount: Int,
    val runDays: Int
)
