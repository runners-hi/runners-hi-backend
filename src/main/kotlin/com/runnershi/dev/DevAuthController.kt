package com.runnershi.dev

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.auth.dto.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class DevLoginRequest(
    @field:NotBlank(message = "userKey는 필수입니다")
    @field:Size(max = 40, message = "userKey는 40자 이하여야 합니다")
    val userKey: String,

    // false면 가입 직후 상태(약관 동의 전, 지역 없음)로 만들어 온보딩 화면을 테스트할 수 있다
    val onboarded: Boolean = true
)

/** 개발(dev) 프로필에서만 열리는 테스트 로그인. 상용에는 이 컨트롤러가 없어 404다. */
@Tag(name = "Dev", description = "개발 서버 전용")
@RestController
@RequestMapping("/api/auth")
@Profile("dev")
class DevAuthController(
    private val devAuthService: DevAuthService
) {

    @Operation(summary = "개발 로그인", description = "userKey로 소셜 로그인 없이 토큰을 받는다. seed01~seed20은 시드 회원, 그 밖의 키는 새 회원")
    @PostMapping("/dev")
    fun devLogin(@Valid @RequestBody request: DevLoginRequest): ApiResponse<AuthResponse> {
        return ApiResponse.success(devAuthService.login(request.userKey.trim(), request.onboarded))
    }
}
