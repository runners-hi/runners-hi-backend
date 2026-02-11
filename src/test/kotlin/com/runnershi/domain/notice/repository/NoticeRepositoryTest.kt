package com.runnershi.domain.notice.repository

import com.runnershi.domain.notice.entity.Notice
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoticeRepositoryTest : RepositoryTest() {

    @Autowired lateinit var noticeRepository: NoticeRepository

    @BeforeEach
    fun setUp() {
        noticeRepository.saveAll(listOf(
            Notice(title = "공지1", content = "내용1", isActive = true),
            Notice(title = "공지2", content = "내용2", isActive = true),
            Notice(title = "공지3", content = "내용3", isActive = true),
            Notice(title = "비활성 공지", content = "내용4", isActive = false),
            Notice(title = "공지5", content = "내용5", isActive = true)
        ))
    }

    @Test
    @DisplayName("활성 공지사항 커서 없이 조회 - 최신순")
    fun findActiveWithoutCursor() {
        val result = noticeRepository.findActiveWithCursor(null, PageRequest.of(0, 10))

        assertEquals(4, result.size) // isActive=false 제외
        // id 내림차순 정렬 확인
        assertTrue(result[0].id > result[1].id)
    }

    @Test
    @DisplayName("활성 공지사항 커서 기반 조회")
    fun findActiveWithCursor() {
        val all = noticeRepository.findActiveWithCursor(null, PageRequest.of(0, 10))
        val cursor = all[1].id // 두 번째 항목의 ID를 커서로

        val result = noticeRepository.findActiveWithCursor(cursor, PageRequest.of(0, 10))

        assertTrue(result.all { it.id < cursor })
    }

    @Test
    @DisplayName("페이지 사이즈 제한 적용")
    fun pageSizeLimit() {
        val result = noticeRepository.findActiveWithCursor(null, PageRequest.of(0, 2))

        assertEquals(2, result.size)
    }

    @Test
    @DisplayName("비활성 공지사항 제외")
    fun excludeInactive() {
        val result = noticeRepository.findActiveWithCursor(null, PageRequest.of(0, 10))

        assertTrue(result.all { it.isActive })
        assertTrue(result.none { it.title == "비활성 공지" })
    }

    @Test
    @DisplayName("공지사항이 없으면 빈 목록 반환")
    fun empty() {
        noticeRepository.deleteAll()

        val result = noticeRepository.findActiveWithCursor(null, PageRequest.of(0, 10))

        assertEquals(0, result.size)
    }
}
