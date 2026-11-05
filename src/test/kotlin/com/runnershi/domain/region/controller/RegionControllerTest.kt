package com.runnershi.domain.region.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.region.dto.DistrictResponse
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.entity.RegionType
import com.runnershi.domain.region.service.RegionService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RegionControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var regionService: RegionService

    @Nested
    @DisplayName("GET /api/regions")
    inner class GetAllRegions {

        @Test
        @DisplayName("지역 목록 조회 성공")
        fun success() {
            whenever(regionService.getAllRegions()).thenReturn(
                listOf(
                    RegionResponse(1L, "서울특별시", RegionType.SPECIAL_CITY),
                    RegionResponse(2L, "부산광역시", RegionType.METROPOLITAN_CITY)
                )
            )

            mockMvc.perform(get("/api/regions"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("서울특별시"))
        }
    }

    @Nested
    @DisplayName("GET /api/regions/{regionId}/districts")
    inner class GetDistricts {

        @Test
        @DisplayName("세부 지역 목록 조회 성공")
        fun success() {
            whenever(regionService.getDistricts(1L)).thenReturn(
                listOf(
                    DistrictResponse(10L, 1L, "강남구"),
                    DistrictResponse(11L, 1L, "강동구")
                )
            )

            mockMvc.perform(get("/api/regions/1/districts"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("강남구"))
        }

        @Test
        @DisplayName("존재하지 않는 지역이면 404 반환")
        fun regionNotFound() {
            whenever(regionService.getDistricts(999L))
                .thenThrow(BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 지역입니다"))

            mockMvc.perform(get("/api/regions/999/districts"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("C003"))
        }
    }
}
