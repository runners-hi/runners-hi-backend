package com.runnershi.common.storage

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

/** 저장소 키가 없는 환경에서도 서버가 뜨도록 처음 쓸 때 만든다. */
@Configuration
class StorageConfig(private val properties: StorageProperties) {

    private val region = Region.of("auto")

    private val pathStyle = S3Configuration.builder().pathStyleAccessEnabled(true).build()

    private fun credentials() = StaticCredentialsProvider.create(
        AwsBasicCredentials.create(properties.accessKey, properties.secretKey)
    )

    @Bean(destroyMethod = "close")
    @Lazy
    fun s3Presigner(): S3Presigner = S3Presigner.builder()
        .region(region)
        .endpointOverride(URI(properties.endpoint))
        .credentialsProvider(credentials())
        .serviceConfiguration(pathStyle)
        .build()

    @Bean(destroyMethod = "close")
    @Lazy
    fun s3Client(): S3Client = S3Client.builder()
        .region(region)
        .endpointOverride(URI(properties.endpoint))
        .credentialsProvider(credentials())
        .serviceConfiguration(pathStyle)
        // R2는 AWS SDK 기본 체크섬 헤더 일부를 지원하지 않는다.
        .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
        .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
        .build()
}
