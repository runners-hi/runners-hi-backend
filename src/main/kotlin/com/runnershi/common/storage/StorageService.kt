package com.runnershi.common.storage

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
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

    fun deleteObject(objectKey: String) {
        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(properties.bucketName)
                .key(objectKey)
                .build()
        )
    }
}
