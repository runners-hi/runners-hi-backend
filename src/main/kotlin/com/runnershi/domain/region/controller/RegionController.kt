package com.runnershi.domain.region.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.region.dto.DistrictResponse
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.service.RegionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Region", description = "지역 API")
@RestController
@RequestMapping("/api/regions")
class RegionController(
    private val regionService: RegionService
) {

    @Operation(summary = "지역 목록 조회", description = "전체 지역(시) 목록 조회 (가나다순)")
    @GetMapping
    fun getAllRegions(): ApiResponse<List<RegionResponse>> {
        val response = regionService.getAllRegions()
        return ApiResponse.success(response)
    }

    @Operation(summary = "세부 지역 목록 조회", description = "선택한 지역의 구/군 목록 조회")
    @GetMapping("/{regionId}/districts")
    fun getDistricts(
        @PathVariable regionId: Long
    ): ApiResponse<List<DistrictResponse>> {
        val response = regionService.getDistricts(regionId)
        return ApiResponse.success(response)
    }
}
