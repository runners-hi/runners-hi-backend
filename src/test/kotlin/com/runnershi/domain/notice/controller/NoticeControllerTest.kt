package com.runnershi.domain.notice.controller

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.notice.dto.NoticeDetailResponse
import com.runnershi.domain.notice.dto.NoticeListCursorResponse
import com.runnershi.domain.notice.dto.NoticeListResponse
import com.runnershi.domain.notice.service.NoticeService
import com.runnershi.support.ControllerTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class NoticeControllerTest : ControllerTest() {

    @MockitoBean
    private lateinit var noticeService: NoticeService

    @Nested
    @DisplayName("GET /api/notices")
    inner class GetNotices {

        @Test
        @DisplayName("공지사항 목록 조회 성공")
        fun success() {
            val notices = listOf(
                NoticeListResponse(2L, "두 번째 공지", LocalDateTime.of(2025, 1, 2, 12, 0)),
                NoticeListResponse(1L, "첫 번째 공지", LocalDateTime.of(2025, 1, 1, 12, 0))
            )
            val response = NoticeListCursorResponse(notices, null, false)
            whenever(noticeService.getNotices(anyOrNull(), any())).thenReturn(response)

            mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notices.length()").value(2))
                .andExpect(jsonPath("$.data.notices[0].title").value("두 번째 공지"))
                .andExpect(jsonPath("$.data.hasNext").value(false))
        }

        @Test
        @DisplayName("커서 파라미터와 size 파라미터 전달")
        fun withParams() {
            val response = NoticeListCursorResponse(emptyList(), null, false)
            whenever(noticeService.getNotices(anyOrNull(), any())).thenReturn(response)

            mockMvc.perform(
                get("/api/notices")
                    .param("cursor", "10")
                    .param("size", "5")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.notices.length()").value(0))
        }
    }

    @Nested
    @DisplayName("GET /api/notices/{noticeId}")
    inner class GetNoticeDetail {

        @Test
        @DisplayName("공지사항 상세 조회 성공")
        fun success() {
            val response = NoticeDetailResponse(1L, "중요 공지", "상세 내용입니다", LocalDateTime.of(2025, 1, 1, 12, 0))
            whenever(noticeService.getNoticeDetail(any())).thenReturn(response)

            mockMvc.perform(get("/api/notices/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("중요 공지"))
                .andExpect(jsonPath("$.data.content").value("상세 내용입니다"))
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 - 에러 반환")
        fun notFound() {
            whenever(noticeService.getNoticeDetail(any()))
                .thenThrow(BusinessException(ErrorCode.NOTICE_NOT_FOUND))

            mockMvc.perform(get("/api/notices/999"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.error.code").value("N001"))
        }
    }
}
