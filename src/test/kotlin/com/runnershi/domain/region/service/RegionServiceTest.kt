package com.runnershi.domain.region.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.region.entity.District
import com.runnershi.domain.region.entity.Region
import com.runnershi.domain.region.entity.RegionType
import com.runnershi.domain.region.repository.DistrictRepository
import com.runnershi.domain.region.repository.RegionRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegionServiceTest {

    @Mock
    lateinit var regionRepository: RegionRepository

    @Mock
    lateinit var districtRepository: DistrictRepository

    private lateinit var regionService: RegionService

    @BeforeEach
    fun setUp() {
        regionService = RegionService(regionRepository, districtRepository)
    }

    private fun createRegion(id: Long = 1L, name: String = "서울특별시"): Region {
        val region = Region(name = name, type = RegionType.SPECIAL_CITY)
        ReflectionTestUtils.setField(region, "id", id)
        return region
    }

    private fun createDistrict(
        id: Long,
        regionId: Long,
        name: String,
        isDefault: Boolean = false
    ): District {
        val district = District(regionId = regionId, name = name, isDefault = isDefault)
        ReflectionTestUtils.setField(district, "id", id)
        return district
    }

    @Nested
    @DisplayName("getAllRegions")
    inner class GetAllRegions {

        @Test
        @DisplayName("지역 목록 조회 성공")
        fun success() {
            whenever(regionRepository.findAllByOrderByNameAsc()).thenReturn(
                listOf(
                    createRegion(1L, "서울특별시"),
                    Region(name = "부산광역시", type = RegionType.METROPOLITAN_CITY)
                )
            )

            val result = regionService.getAllRegions()

            assertEquals(2, result.size)
            assertEquals("서울특별시", result[0].name)
        }
    }

    @Nested
    @DisplayName("getDistricts")
    inner class GetDistricts {

        @Test
        @DisplayName("실제 구/군 데이터가 있으면 기본 district는 숨김")
        fun hideDefaultDistrict() {
            whenever(regionRepository.existsById(1L)).thenReturn(true)
            whenever(districtRepository.findAllByRegionIdOrderByIsDefaultAscNameAsc(1L)).thenReturn(
                listOf(
                    createDistrict(10L, 1L, "강남구"),
                    createDistrict(11L, 1L, "강동구"),
                    createDistrict(1L, 1L, "서울특별시", isDefault = true)
                )
            )

            val result = regionService.getDistricts(1L)

            assertEquals(2, result.size)
            assertEquals("강남구", result[0].name)
            assertEquals("강동구", result[1].name)
        }

        @Test
        @DisplayName("실제 구/군 데이터가 없으면 기본 district 반환")
        fun returnDefaultDistrictOnly() {
            whenever(regionRepository.existsById(2L)).thenReturn(true)
            whenever(districtRepository.findAllByRegionIdOrderByIsDefaultAscNameAsc(2L)).thenReturn(
                listOf(createDistrict(20L, 2L, "과천시", isDefault = true))
            )

            val result = regionService.getDistricts(2L)

            assertEquals(1, result.size)
            assertEquals("과천시", result[0].name)
        }

        @Test
        @DisplayName("존재하지 않는 지역이면 예외 발생")
        fun regionNotFound() {
            whenever(regionRepository.existsById(999L)).thenReturn(false)

            val exception = assertThrows<BusinessException> {
                regionService.getDistricts(999L)
            }

            assertEquals(ErrorCode.RESOURCE_NOT_FOUND, exception.errorCode)
        }
    }
}
