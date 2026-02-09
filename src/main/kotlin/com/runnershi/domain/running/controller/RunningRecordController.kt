package com.runnershi.domain.running.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.service.RunningRecordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Running", description = "러닝 기록 API")
@RestController
@RequestMapping("/api/running-records")
class RunningRecordController(
    private val runningRecordService: RunningRecordService
) {

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
