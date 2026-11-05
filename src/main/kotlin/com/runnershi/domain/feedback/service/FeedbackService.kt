package com.runnershi.domain.feedback.service

import com.runnershi.domain.feedback.entity.Feedback
import com.runnershi.domain.feedback.repository.FeedbackRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedbackService(
    private val feedbackRepository: FeedbackRepository
) {

    @Transactional
    fun create(userId: Long, content: String) {
        feedbackRepository.save(Feedback(userId = userId, content = content.trim()))
    }
}
