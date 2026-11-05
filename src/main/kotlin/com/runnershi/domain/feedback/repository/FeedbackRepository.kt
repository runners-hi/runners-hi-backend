package com.runnershi.domain.feedback.repository

import com.runnershi.domain.feedback.entity.Feedback
import org.springframework.data.jpa.repository.JpaRepository

interface FeedbackRepository : JpaRepository<Feedback, Long> {
    fun deleteAllByUserId(userId: Long)
}
