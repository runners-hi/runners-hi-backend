package com.runnershi.domain.feedback.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class FeedbackCreateRequest(
    @field:NotBlank(message = "내용을 입력해 주세요")
    @field:Size(max = 1000, message = "내용은 1000자 이하로 입력해 주세요")
    val content: String
)
