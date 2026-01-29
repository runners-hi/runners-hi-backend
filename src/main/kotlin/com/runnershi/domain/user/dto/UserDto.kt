package com.runnershi.domain.user.dto

import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.user.entity.User

data class MyProfileResponse(
    val nickname: String,
    val profileImageUrl: String?,
    val region: RegionResponse?,
    val notificationEnabled: Boolean
) {
    companion object {
        fun from(user: User, region: RegionResponse?) = MyProfileResponse(
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            region = region,
            notificationEnabled = user.notificationEnabled
        )
    }
}
