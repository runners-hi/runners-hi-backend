package com.runnershi.domain.user.dto

import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.user.entity.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class NicknameUpdateRequest(
    @field:NotBlank(message = "닉네임은 필수입니다")
    val nickname: String
)

// BE-033: 알림 설정 변경
data class NotificationSettingsUpdateRequest(
    @field:NotNull(message = "푸시 알림 설정은 필수입니다")
    val notificationEnabled: Boolean,

    @field:NotNull(message = "마케팅 알림 설정은 필수입니다")
    val marketingNotificationEnabled: Boolean
)

// BE-096: 프로필 이미지 변경 (GCS 업로드 후 클라이언트가 최종 URL 전달)
data class ProfileImageUpdateRequest(
    @field:NotBlank(message = "이미지 URL은 필수입니다")
    val profileImageUrl: String
)

// BE-082: FCM 토큰 등록/갱신
data class FcmTokenUpdateRequest(
    @field:NotBlank(message = "FCM 토큰은 필수입니다")
    val fcmToken: String
)

data class NicknameCheckResponse(
    val available: Boolean
)

data class MyProfileResponse(
    val nickname: String,
    val profileImageUrl: String?,
    val region: RegionResponse?,
    val notificationEnabled: Boolean,
    val marketingNotificationEnabled: Boolean
) {
    companion object {
        fun from(user: User, region: RegionResponse?) = MyProfileResponse(
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            region = region,
            notificationEnabled = user.notificationEnabled,
            marketingNotificationEnabled = user.marketingNotificationEnabled
        )
    }
}
