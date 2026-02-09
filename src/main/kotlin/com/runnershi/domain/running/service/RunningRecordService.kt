package com.runnershi.domain.running.service

import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.repository.RunningRecordRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RunningRecordService(
    private val runningRecordRepository: RunningRecordRepository
) {

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
