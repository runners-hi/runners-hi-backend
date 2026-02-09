package com.runnershi.domain.mission.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.mission.dto.HomeMissionResponse
import com.runnershi.domain.mission.dto.MissionGroupResponse
import com.runnershi.domain.mission.dto.MyMissionSummaryResponse
import com.runnershi.domain.mission.service.MissionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Mission", description = "미션 API")
@RestController
@RequestMapping("/api/missions")
class MissionController(
    private val missionService: MissionService
) {

    @Operation(summary = "미션 목록 조회", description = "전체 미션 그룹 및 미션 목록 조회 (미션 탭)")
    @GetMapping
    fun getMissionGroups(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<List<MissionGroupResponse>> {
        val response = missionService.getMissionGroups(userId)
        return ApiResponse.success(response)
    }

    @Operation(summary = "홈 화면 미션 조회", description = "홈 화면에 표시할 활성 미션 이벤트 조회")
    @GetMapping("/home")
    fun getHomeMissions(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<HomeMissionResponse?> {
        val response = missionService.getHomeMissions(userId)
        return ApiResponse.success(response)
    }

    @Operation(summary = "미션 그룹 상세 조회", description = "특정 미션 그룹의 미션 목록 및 달성 현황 조회")
    @GetMapping("/groups/{groupId}")
    fun getMissionGroupDetail(
        @AuthenticationPrincipal userId: Long,
        @PathVariable groupId: Long
    ): ApiResponse<MissionGroupResponse> {
        val response = missionService.getMissionGroupDetail(userId, groupId)
        return ApiResponse.success(response)
    }

    @Operation(summary = "내 미션 현황 조회", description = "달성/진행 카운트 및 최근 달성 미션 목록")
    @GetMapping("/me")
    fun getMyMissionSummary(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<MyMissionSummaryResponse> {
        val response = missionService.getMyMissionSummary(userId)
        return ApiResponse.success(response)
    }
}
