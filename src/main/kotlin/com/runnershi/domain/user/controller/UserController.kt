package com.runnershi.domain.user.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.region.dto.RegionUpdateRequest
import com.runnershi.domain.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {

    @Operation(summary = "지역 선택/변경", description = "러닝 지역 선택 또는 변경")
    @PatchMapping("/region")
    fun updateRegion(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: RegionUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateRegion(userId, request.regionId)
        return ApiResponse.success(Unit)
    }
}
