package com.runnershi.common.storage

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GcsConfig(
    private val gcsProperties: GcsProperties
) {

    @Bean
    fun storage(): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(gcsProperties.projectId)
            .build()
            .service
    }
}
