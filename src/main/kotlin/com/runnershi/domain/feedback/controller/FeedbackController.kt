package com.runnershi.domain.feedback.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.feedback.dto.FeedbackCreateRequest
import com.runnershi.domain.feedback.service.FeedbackService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Feedback", description = "개발자에게 보내는 의견 API")
@RestController
@RequestMapping("/api/feedback")
class FeedbackController(
    private val feedbackService: FeedbackService
) {

    @Operation(summary = "의견 보내기")
    @PostMapping
    fun create(
        @AuthenticationPrincipal userId: Long,
        @Valid @RequestBody request: FeedbackCreateRequest
    ): ApiResponse<Unit> {
        feedbackService.create(userId, request.content)
        return ApiResponse.success(Unit)
    }
}
