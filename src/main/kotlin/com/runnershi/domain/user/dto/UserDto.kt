package com.runnershi.domain.user.dto

import com.runnershi.domain.region.dto.DistrictResponse
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.user.entity.User
import jakarta.validation.constraints.NotBlank

data class NicknameUpdateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    val nickname: String
)

data class NicknameCheckResponse(
    val available: Boolean
)

data class MyProfileResponse(
    val nickname: String,
    val profileImageUrl: String?,
    val region: RegionResponse?,
    val district: DistrictResponse?,
    val notificationEnabled: Boolean
) {
    companion object {
        fun from(user: User, region: RegionResponse?, district: DistrictResponse?) = MyProfileResponse(
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            region = region,
            district = district,
            notificationEnabled = user.notificationEnabled
        )
    }
}
