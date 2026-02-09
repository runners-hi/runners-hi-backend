package com.runnershi.domain.notice.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.notice.dto.NoticeDetailResponse
import com.runnershi.domain.notice.dto.NoticeListCursorResponse
import com.runnershi.domain.notice.dto.NoticeListResponse
import com.runnershi.domain.notice.repository.NoticeRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository
) {

    @Transactional(readOnly = true)
    fun getNotices(cursor: Long?, size: Int): NoticeListCursorResponse {
        val pageable = PageRequest.of(0, size + 1)
        val notices = noticeRepository.findActiveWithCursor(cursor, pageable)

        val hasNext = notices.size > size
        val content = if (hasNext) notices.dropLast(1) else notices

        return NoticeListCursorResponse(
            notices = content.map { NoticeListResponse.from(it) },
            nextCursor = if (hasNext) content.last().id else null,
            hasNext = hasNext
        )
    }

    @Transactional(readOnly = true)
    fun getNoticeDetail(noticeId: Long): NoticeDetailResponse {
        val notice = noticeRepository.findById(noticeId)
            .orElseThrow { BusinessException(ErrorCode.NOTICE_NOT_FOUND) }
        return NoticeDetailResponse.from(notice)
    }
}
