package com.runnershi.domain.notice.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.notice.entity.Notice
import com.runnershi.domain.notice.repository.NoticeRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NoticeServiceTest {

    @Mock lateinit var noticeRepository: NoticeRepository

    private lateinit var noticeService: NoticeService

    @BeforeEach
    fun setUp() {
        noticeService = NoticeService(noticeRepository)
    }

    private fun createNotice(id: Long, title: String = "공지사항 $id", content: String = "내용 $id"): Notice {
        val notice = Notice(title = title, content = content)
        ReflectionTestUtils.setField(notice, "id", id)
        return notice
    }

    @Nested
    @DisplayName("getNotices")
    inner class GetNotices {

        @Test
        @DisplayName("공지사항 목록 조회 성공 - 다음 페이지 있음")
        fun hasNext() {
            val notices = (1..3).map { createNotice(it.toLong()) }
            whenever(noticeRepository.findActiveWithCursor(anyOrNull(), any())).thenReturn(notices)

            val response = noticeService.getNotices(null, 2)

            assertEquals(2, response.notices.size)
            assertTrue(response.hasNext)
            assertNotNull(response.nextCursor)
        }

        @Test
        @DisplayName("공지사항 목록 조회 - 마지막 페이지")
        fun lastPage() {
            val notices = listOf(createNotice(1L))
            whenever(noticeRepository.findActiveWithCursor(anyOrNull(), any())).thenReturn(notices)

            val response = noticeService.getNotices(null, 2)

            assertEquals(1, response.notices.size)
            assertFalse(response.hasNext)
            assertNull(response.nextCursor)
        }

        @Test
        @DisplayName("공지사항이 없으면 빈 목록 반환")
        fun empty() {
            whenever(noticeRepository.findActiveWithCursor(anyOrNull(), any())).thenReturn(emptyList())

            val response = noticeService.getNotices(null, 20)

            assertEquals(0, response.notices.size)
            assertFalse(response.hasNext)
        }

        @Test
        @DisplayName("커서 기반 페이지네이션 - 커서 전달")
        fun withCursor() {
            val notices = listOf(createNotice(5L), createNotice(4L))
            whenever(noticeRepository.findActiveWithCursor(anyOrNull(), any())).thenReturn(notices)

            val response = noticeService.getNotices(10L, 2)

            assertEquals(2, response.notices.size)
            assertFalse(response.hasNext)
        }
    }

    @Nested
    @DisplayName("getNoticeDetail")
    inner class GetNoticeDetail {

        @Test
        @DisplayName("공지사항 상세 조회 성공")
        fun success() {
            val notice = createNotice(1L, "중요 공지", "상세 내용입니다")
            whenever(noticeRepository.findById(1L)).thenReturn(Optional.of(notice))

            val response = noticeService.getNoticeDetail(1L)

            assertEquals(1L, response.id)
            assertEquals("중요 공지", response.title)
            assertEquals("상세 내용입니다", response.content)
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 - 예외 발생")
        fun notFound() {
            whenever(noticeRepository.findById(999L)).thenReturn(Optional.empty())

            val exception = assertThrows<BusinessException> {
                noticeService.getNoticeDetail(999L)
            }
            assertEquals(ErrorCode.NOTICE_NOT_FOUND, exception.errorCode)
        }
    }
}
