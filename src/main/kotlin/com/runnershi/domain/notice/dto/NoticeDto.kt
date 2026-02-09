package com.runnershi.domain.notice.dto

import com.runnershi.domain.notice.entity.Notice
import java.time.LocalDateTime

data class NoticeListResponse(
    val id: Long,
    val title: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notice: Notice) = NoticeListResponse(
            id = notice.id,
            title = notice.title,
            createdAt = notice.createdAt
        )
    }
}

data class NoticeListCursorResponse(
    val notices: List<NoticeListResponse>,
    val nextCursor: Long?,
    val hasNext: Boolean
)

data class NoticeDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notice: Notice) = NoticeDetailResponse(
            id = notice.id,
            title = notice.title,
            content = notice.content,
            createdAt = notice.createdAt
        )
    }
}
