package com.runnershi.domain.user.dto

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

data class ProfileImageUploadRequest(
    @field:NotBlank(message = "콘텐츠 타입은 필수입니다")
    val contentType: String
)

data class ProfileImageUploadResponse(
    val presignedUrl: String,
    val objectKey: String
)

data class ProfileImageConfirmRequest(
    @field:NotBlank(message = "오브젝트 키는 필수입니다")
    val objectKey: String
)

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
