package com.runnershi.domain.feedback

import com.runnershi.domain.feedback.entity.Feedback
import com.runnershi.domain.feedback.repository.FeedbackRepository
import com.runnershi.domain.feedback.service.FeedbackService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class FeedbackServiceTest {

    @Mock lateinit var feedbackRepository: FeedbackRepository
    @InjectMocks lateinit var feedbackService: FeedbackService

    @Test
    fun `앞뒤 공백을 지우고 저장한다`() {
        feedbackService.create(1L, "  좋아요  ")

        val captor = ArgumentCaptor.forClass(Feedback::class.java)
        verify(feedbackRepository).save(captor.capture())
        assertEquals(1L, captor.value.userId)
        assertEquals("좋아요", captor.value.content)
    }
}
