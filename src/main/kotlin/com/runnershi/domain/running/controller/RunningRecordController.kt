package com.runnershi.domain.running.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.running.dto.DailyRecordResponse
import com.runnershi.domain.running.dto.RunningRecordCreateRequest
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.dto.WeeklySummaryResponse
import com.runnershi.domain.running.service.RunningRecordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Running", description = "러닝 기록 API")
@RestController
@RequestMapping("/api/running-records")
class RunningRecordController(
    private val runningRecordService: RunningRecordService
) {

    @Operation(summary = "러닝 기록 저장", description = "러닝 기록 저장 (pace, runningDate 서버 계산)")
    @PostMapping
    fun createRunningRecord(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: RunningRecordCreateRequest
    ): ApiResponse<RunningRecordResponse> {
        val response = runningRecordService.createRunningRecord(userId, request)
        return ApiResponse.success(response)
    }

    @Operation(summary = "주간 요약 조회", description = "해당 날짜가 속한 주(월~일)의 러닝 요약 (기본값: 이번 주)")
    @GetMapping("/weekly")
    fun getWeeklySummary(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ApiResponse<WeeklySummaryResponse> {
        val response = runningRecordService.getWeeklySummary(userId, date ?: LocalDate.now())
        return ApiResponse.success(response)
    }

    @Operation(summary = "일일 기록 조회", description = "특정 날짜의 러닝 기록 합산 및 개별 기록 조회 (기본값: 오늘)")
    @GetMapping("/daily")
    fun getDailyRecord(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ApiResponse<DailyRecordResponse> {
        val response = runningRecordService.getDailyRecord(userId, date ?: LocalDate.now())
        return ApiResponse.success(response)
    }

    @Operation(summary = "러닝 기록 목록 조회", description = "내 러닝 기록 목록을 커서 기반 페이징 조회")
    @GetMapping
    fun getRunningRecords(
        @AuthenticationPrincipal userId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<RunningRecordListResponse> {
        val response = runningRecordService.getRunningRecords(userId, cursor, size)
        return ApiResponse.success(response)
    }
}
