package com.runnershi.domain.user.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.region.dto.RegionUpdateRequest
import com.runnershi.domain.user.dto.MyProfileResponse
import com.runnershi.domain.user.dto.NicknameCheckResponse
import com.runnershi.domain.user.dto.NicknameUpdateRequest
import com.runnershi.domain.user.dto.NotificationSettingsUpdateRequest
import com.runnershi.domain.user.dto.ProfileImageUpdateRequest
import com.runnershi.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
        userService.updateRegion(userId, request.regionId)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "알림 설정 변경", description = "푸시 알림 및 마케팅 알림 설정 변경")
    @PatchMapping("/notification-settings")
    fun updateNotificationSettings(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: NotificationSettingsUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateNotificationSettings(userId, request)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "프로필 이미지 변경", description = "GCS 업로드 후 최종 이미지 URL 저장")
    @PatchMapping("/profile-image")
    fun updateProfileImage(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: ProfileImageUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateProfileImage(userId, request.profileImageUrl)
        return ApiResponse.success(Unit)
    }

    @Operation(summary = "계정 탈퇴", description = "계정 탈퇴 (정책 확정 후 개인정보 처리 추가 예정)")
    @DeleteMapping("/me")
    fun withdraw(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<Unit> {
        userService.withdraw(userId)
        return ApiResponse.success(Unit)
    }
}
