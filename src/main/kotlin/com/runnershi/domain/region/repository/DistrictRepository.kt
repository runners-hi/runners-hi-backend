package com.runnershi.domain.region.repository

import com.runnershi.domain.region.entity.District
import org.springframework.data.jpa.repository.JpaRepository

interface DistrictRepository : JpaRepository<District, Long> {
    fun findAllByRegionIdOrderByIsDefaultAscNameAsc(regionId: Long): List<District>
    fun findByIdAndRegionId(districtId: Long, regionId: Long): District?
    fun findFirstByRegionIdAndIsDefaultTrue(regionId: Long): District?
}
