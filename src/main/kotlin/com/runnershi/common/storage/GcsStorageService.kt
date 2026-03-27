package com.runnershi.common.storage

import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import com.runnershi.common.config.GcsProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

// BE-036: GCS Presigned URL 발급 서비스
//
// 업로드 흐름:
//   1. 클라이언트 → GET /api/storage/presigned-url?type=profile-image
//   2. 서버 → GCS Presigned URL + 최종 objectUrl 반환
//   3. 클라이언트 → Presigned URL로 직접 PUT 업로드 (서버 미경유)
//   4. 클라이언트 → PATCH /api/users/profile-image { profileImageUrl: objectUrl }
@Service
@Profile("!test")
class GcsStorageService(
    private val storage: Storage,
    private val gcsProperties: GcsProperties
) {

    fun generateProfileImagePresignedUrl(userId: Long): PresignedUrlResponse {
        val objectName = "profile-images/$userId/${UUID.randomUUID()}.jpg"
        val blobInfo = BlobInfo.newBuilder(gcsProperties.bucketName, objectName)
            .setContentType("image/jpeg")
            .build()

        val presignedUrl = storage.signUrl(
            blobInfo,
            gcsProperties.presignedUrlExpirationMinutes,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withV4Signature()
        )

        val objectUrl = "https://storage.googleapis.com/${gcsProperties.bucketName}/$objectName"

        return PresignedUrlResponse(
            presignedUrl = presignedUrl.toString(),
            objectUrl = objectUrl,
            expiresInMinutes = gcsProperties.presignedUrlExpirationMinutes
        )
    }
}

data class PresignedUrlResponse(
    val presignedUrl: String,
    val objectUrl: String,
    val expiresInMinutes: Long
)
