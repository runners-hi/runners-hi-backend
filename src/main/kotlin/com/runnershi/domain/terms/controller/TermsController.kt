package com.runnershi.domain.terms.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.terms.dto.TermsAgreementRequest
import com.runnershi.domain.terms.dto.TermsResponse
import com.runnershi.domain.terms.service.TermsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Terms", description = "약관 API")
@RestController
@RequestMapping("/api/terms")
class TermsController(
    private val termsService: TermsService
) {

    @Operation(summary = "약관 목록 조회", description = "서비스 이용약관 목록 조회 (로그인 시 동의 여부 포함)")
    @GetMapping
    fun getTermsList(
        @AuthenticationPrincipal userId: Long?
    ): ApiResponse<List<TermsResponse>> {
        val response = termsService.getTermsList(userId)
        return ApiResponse.success(response)
    }

    @Operation(summary = "약관 동의", description = "서비스 이용약관 동의")
    @PostMapping("/agree")
    fun agreeToTerms(
        @AuthenticationPrincipal userId: Long,
        @RequestBody request: TermsAgreementRequest
    ): ApiResponse<Unit> {
        termsService.agreeToTerms(userId, request)
        return ApiResponse.success(Unit)
    }

    // TODO: 선택 약관(마케팅, SNS) 동의 철회 API 필요 여부 확인
}
