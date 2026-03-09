package com.runnershi.domain.region.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.region.dto.DistrictResponse
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.repository.DistrictRepository
import com.runnershi.domain.region.repository.RegionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegionService(
    private val regionRepository: RegionRepository,
    private val districtRepository: DistrictRepository
) {

    @Transactional(readOnly = true)
    fun getAllRegions(): List<RegionResponse> {
        return regionRepository.findAllByOrderByNameAsc()
            .map { RegionResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun getDistricts(regionId: Long): List<DistrictResponse> {
        if (!regionRepository.existsById(regionId)) {
            throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 지역입니다")
        }

        val districts = districtRepository.findAllByRegionIdOrderByIsDefaultAscNameAsc(regionId)
        val visibleDistricts = if (districts.any { !it.isDefault }) {
            districts.filter { !it.isDefault }
        } else {
            districts
        }

        return visibleDistricts.map { DistrictResponse.from(it) }
    }
}
