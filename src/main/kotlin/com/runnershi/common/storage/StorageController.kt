package com.runnershi.common.storage

import com.runnershi.common.response.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Storage", description = "파일 업로드 API")
@RestController
@RequestMapping("/api/storage")
class StorageController(
    private val gcsStorageService: GcsStorageService
) {

    @Operation(
        summary = "프로필 이미지 업로드 URL 발급",
        description = "GCS Presigned URL 발급. 클라이언트가 해당 URL로 직접 PUT 업로드 후 objectUrl을 /api/users/profile-image에 저장"
    )
    @GetMapping("/presigned-url/profile-image")
    fun getProfileImagePresignedUrl(
        @AuthenticationPrincipal userId: Long
    ): ApiResponse<PresignedUrlResponse> {
        val response = gcsStorageService.generateProfileImagePresignedUrl(userId)
        return ApiResponse.success(response)
    }
}
