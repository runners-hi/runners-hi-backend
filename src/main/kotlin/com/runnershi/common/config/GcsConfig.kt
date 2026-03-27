package com.runnershi.common.config

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GcsConfig {

    // GCS Storage 빈
    // 로컬/CI: GOOGLE_APPLICATION_CREDENTIALS 환경변수로 서비스 계정 키 경로 지정
    // Cloud Run: Workload Identity 또는 서비스 계정 자동 인증
    @Bean
    fun storage(): Storage = StorageOptions.getDefaultInstance().service
}
