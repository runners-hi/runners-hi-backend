package com.runnershi.domain.app.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.app.dto.AppVersionCheckResponse
import com.runnershi.domain.app.entity.Platform
import com.runnershi.domain.app.service.AppVersionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "App", description = "앱 관리 API")
@RestController
@RequestMapping("/api/app")
class AppVersionController(
    private val appVersionService: AppVersionService
) {

    @Operation(
        summary = "앱 버전 체크",
        description = "현재 앱 버전과 서버 기준 비교. forceUpdate=true면 강제 업데이트 필요"
    )
    @GetMapping("/version/check")
    fun checkVersion(
        @RequestParam platform: Platform,
        @RequestParam version: String
    ): ApiResponse<AppVersionCheckResponse> {
        val response = appVersionService.checkVersion(platform, version)
        return ApiResponse.success(response)
    }
}
