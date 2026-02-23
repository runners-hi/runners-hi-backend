package com.runnershi.common.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "gcs")
data class GcsProperties(
    val bucketName: String,
    val projectId: String
)
