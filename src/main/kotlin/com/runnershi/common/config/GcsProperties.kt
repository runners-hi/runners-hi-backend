package com.runnershi.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gcs")
data class GcsProperties(
    val bucketName: String,
    val presignedUrlExpirationMinutes: Long = 15
)
