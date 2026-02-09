package com.runnershi.domain.notice.repository

import com.runnershi.domain.notice.entity.Notice
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface NoticeRepository : JpaRepository<Notice, Long> {

    @Query("""
        SELECT n FROM Notice n
        WHERE n.isActive = true
        AND (:cursor IS NULL OR n.id < :cursor)
        ORDER BY n.id DESC
    """)
    fun findActiveWithCursor(cursor: Long?, pageable: Pageable): List<Notice>
}
