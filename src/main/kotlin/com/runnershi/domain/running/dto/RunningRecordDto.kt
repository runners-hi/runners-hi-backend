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
