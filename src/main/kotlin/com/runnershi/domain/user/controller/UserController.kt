package com.runnershi.domain.user.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.region.dto.RegionUpdateRequest
import com.runnershi.domain.user.dto.FcmTokenUpdateRequest
import com.runnershi.domain.user.dto.MyProfileResponse
import com.runnershi.domain.user.dto.NotificationSettingsUpdateRequest
import com.runnershi.domain.user.dto.NicknameCheckResponse
import com.runnershi.domain.user.dto.NicknameUpdateRequest
import com.runnershi.domain.user.dto.ProfileImageConfirmRequest
import com.runnershi.domain.user.dto.ProfileImageUploadRequest
import com.runnershi.domain.user.dto.ProfileImageUploadResponse
import com.runnershi.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "닉네임 변경", description = "사용자 닉네임 변경")
    @PatchMapping("/nickname")
    fun updateNickname(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: NicknameUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateNickname(userId, request.nickname)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "닉네임 중복 체크", description = "닉네임 사용 가능 여부 확인")
    @GetMapping("/nickname/check")
    fun checkNickname(
        @AuthenticationPrincipal userId: Long,
        @RequestParam nickname: String
    ): ApiResponse<NicknameCheckResponse> {
        val available = userService.checkNicknameAvailable(nickname)
        return ApiResponse.success(NicknameCheckResponse(available))
    }

    @Operation(summary = "내 정보 조회", description = "마이페이지 내 정보 조회")
    @GetMapping("/me")
    fun getMyProfile(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<MyProfileResponse> {
        val response = userService.getMyProfile(userId)
        return ApiResponse.success(response)
    }

    @Operation(summary = "지역 선택/변경", description = "러닝 지역 선택 또는 변경")
    @PatchMapping("/region")
    fun updateRegion(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: RegionUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateRegion(userId, request.regionId, request.districtId)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "프로필 이미지 업로드 URL 발급", description = "저장소 Presigned URL을 발급하여 클라이언트가 직접 이미지를 업로드할 수 있도록 합니다")
    @PostMapping("/me/profile-image/upload-url")
    fun getProfileImageUploadUrl(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: ProfileImageUploadRequest
    ): ApiResponse<ProfileImageUploadResponse> {
        val response = userService.generateProfileImageUploadUrl(userId, request.contentType)
        return ApiResponse.success(response)
    }

    @Operation(summary = "프로필 이미지 확정", description = "이미지 업로드 완료 후 DB에 object key를 저장합니다")
    @PatchMapping("/me/profile-image")
    fun confirmProfileImage(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: ProfileImageConfirmRequest
    ): ApiResponse<Unit> {
        userService.confirmProfileImage(userId, request.objectKey)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "프로필 이미지 삭제", description = "프로필 이미지를 제거합니다")
    @DeleteMapping("/me/profile-image")
    fun removeProfileImage(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Unit> {
        userService.removeProfileImage(userId)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "FCM 토큰 등록", description = "앱 실행 시 FCM 토큰을 저장합니다")
    @PatchMapping("/fcm-token")
    fun updateFcmToken(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FcmTokenUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateFcmToken(userId, request.fcmToken)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "푸시 알림 설정 변경")
    @PatchMapping("/notification-settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: NotificationSettingsUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateNotificationEnabled(userId, request.notificationEnabled)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "계정 탈퇴", description = "계정 탈퇴 및 개인정보 마스킹 처리")
    @DeleteMapping("/me")
    fun withdraw(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Unit> {
        userService.withdraw(userId)
        return ApiResponse.success(Unit)
    }
}
