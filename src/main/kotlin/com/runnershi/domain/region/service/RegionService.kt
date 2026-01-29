package com.runnershi.domain.region.service

import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.repository.RegionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegionService(
    private val regionRepository: RegionRepository
) {

    @Transactional(readOnly = true)
    fun getAllRegions(): List<RegionResponse> {
        return regionRepository.findAllByOrderByNameAsc()
            .map { RegionResponse.from(it) }
    }
}
