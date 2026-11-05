package com.runnershi.common.storage

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.MetadataDirective
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.UUID

data class PresignedUrlInfo(
    val url: String,
    val objectKey: String
)

@Service
class StorageService(
    @Lazy private val presigner: S3Presigner,
    @Lazy private val s3Client: S3Client,
    private val properties: StorageProperties
) {

    companion object {
        private val CONTENT_TYPE_TO_EXT = mapOf(
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp"
        )
        private val PRESIGNED_URL_DURATION: Duration = Duration.ofMinutes(15)
        private const val PROFILE_IMAGE_CACHE_CONTROL = "public, max-age=300"
    }

    fun generatePresignedUploadUrl(userId: Long, contentType: String): PresignedUrlInfo {
        val ext = CONTENT_TYPE_TO_EXT[contentType]
            ?: throw IllegalArgumentException("Unsupported content type: $contentType")

        val objectKey = "profiles/$userId/${UUID.randomUUID()}.$ext"

        val request = PutObjectPresignRequest.builder()
            .signatureDuration(PRESIGNED_URL_DURATION)
            .putObjectRequest(
                PutObjectRequest.builder()
                    .bucket(properties.bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .build()
            )
            .build()

        return PresignedUrlInfo(
            url = presigner.presignPutObject(request).url().toString(),
            objectKey = objectKey
        )
    }

    /**
     * 탈퇴로 지운 사진이 CDN에 오래 남지 않게 5분만 캐시한다.
     * 업로드 서명에 넣으면 앱도 같은 헤더를 보내야 하므로, 올라온 뒤 서버가 같은 키에 덮어써서 붙인다.
     */
    fun applyProfileImageCachePolicy(objectKey: String) {
        val ext = objectKey.substringAfterLast('.')
        val contentType = CONTENT_TYPE_TO_EXT.entries.first { it.value == ext }.key
        s3Client.copyObject(
            CopyObjectRequest.builder()
                .sourceBucket(properties.bucketName)
                .sourceKey(objectKey)
                .destinationBucket(properties.bucketName)
                .destinationKey(objectKey)
                .metadataDirective(MetadataDirective.REPLACE)
                .contentType(contentType)
                .cacheControl(PROFILE_IMAGE_CACHE_CONTROL)
                .build()
        )
    }

    fun deleteObject(objectKey: String) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(properties.bucketName)
                .key(objectKey)
                .build()
        )
    }
}
