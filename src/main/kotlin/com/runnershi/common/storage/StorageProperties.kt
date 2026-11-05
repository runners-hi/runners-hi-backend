package com.runnershi.common.storage

import org.springframework.boot.context.properties.ConfigurationProperties

/** S3 호환 저장소(Cloudflare R2) 설정 */
@ConfigurationProperties(prefix = "storage")
data class StorageProperties(
    val bucketName: String,
    val publicBaseUrl: String = "",
    val endpoint: String = "",
    val accessKey: String = "",
    val secretKey: String = ""
)
