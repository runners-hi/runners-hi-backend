package com.runnershi.domain.region.repository

import com.runnershi.domain.region.entity.Region
import org.springframework.data.jpa.repository.JpaRepository

interface RegionRepository : JpaRepository<Region, Long> {
    fun findAllByOrderByNameAsc(): List<Region>
}
