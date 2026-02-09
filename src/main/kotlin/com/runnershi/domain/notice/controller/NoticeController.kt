package com.runnershi.domain.notice.controller

import com.runnershi.common.response.ApiResponse
import com.runnershi.domain.notice.dto.NoticeDetailResponse
import com.runnershi.domain.notice.dto.NoticeListCursorResponse
import com.runnershi.domain.notice.service.NoticeService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Notice", description = "공지사항 API")
@RestController
@RequestMapping("/api/notices")
class NoticeController(
    private val noticeService: NoticeService
) {

    @Operation(summary = "공지사항 목록 조회", description = "활성 공지사항 목록 커서 기반 페이징 조회 (최신순)")
    @GetMapping
    fun getNotices(
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<NoticeListCursorResponse> {
        val response = noticeService.getNotices(cursor, size)
        return ApiResponse.success(response)
    }

    @Operation(summary = "공지사항 상세 조회", description = "공지사항 상세 내용 조회")
    @GetMapping("/{noticeId}")
    fun getNoticeDetail(
        @PathVariable noticeId: Long
    ): ApiResponse<NoticeDetailResponse> {
        val response = noticeService.getNoticeDetail(noticeId)
        return ApiResponse.success(response)
    }
}
