package com.runnershi.domain.region.dto

import com.runnershi.domain.region.entity.Region
import com.runnershi.domain.region.entity.RegionType

data class RegionResponse(
    val id: Long,
    val name: String,
    val type: RegionType
) {
    companion object {
        fun from(region: Region) = RegionResponse(
            id = region.id,
            name = region.name,
            type = region.type
        )
    }
}

data class RegionUpdateRequest(
    val regionId: Long
)
