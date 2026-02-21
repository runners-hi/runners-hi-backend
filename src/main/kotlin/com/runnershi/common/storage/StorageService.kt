package com.runnershi.common.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.HttpMethod
import com.google.cloud.storage.Storage
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit

data class PresignedUrlInfo(
    val url: String,
    val objectKey: String
)

@Service
class StorageService(
    private val storage: Storage,
    private val gcsProperties: GcsProperties
) {

    companion object {
        private val CONTENT_TYPE_TO_EXT = mapOf(
            "image/jpeg" to "jpg",
            "image/png" to "png",
            "image/webp" to "webp"
        )
        private const val PRESIGNED_URL_DURATION_MINUTES = 15L
    }

    fun generatePresignedUploadUrl(userId: Long, contentType: String): PresignedUrlInfo {
        val ext = CONTENT_TYPE_TO_EXT[contentType]
            ?: throw IllegalArgumentException("Unsupported content type: $contentType")

        val objectKey = "profiles/$userId/${UUID.randomUUID()}.$ext"

        val blobInfo = BlobInfo.newBuilder(BlobId.of(gcsProperties.bucketName, objectKey))
            .setContentType(contentType)
            .build()

        val url = storage.signUrl(
            blobInfo,
            PRESIGNED_URL_DURATION_MINUTES,
            TimeUnit.MINUTES,
            Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
            Storage.SignUrlOption.withContentType()
        )

        return PresignedUrlInfo(
            url = url.toString(),
            objectKey = objectKey
        )
    }

    fun deleteObject(objectKey: String) {
        val blobId = BlobId.of(gcsProperties.bucketName, objectKey)
        storage.delete(blobId)
    }
}
