package com.runnershi.common.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.MetadataDirective
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

class StorageServiceTest {

    private val presigner = S3Presigner.builder()
        .region(Region.of("auto"))
        .endpointOverride(URI("https://storage.example.com"))
        .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("key", "secret")))
        .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
        .build()
    private val s3Client: S3Client = mock()
    private val service = StorageService(presigner, s3Client, StorageProperties("bucket"))

    @Test
    fun `업로드 서명에는 앱이 보내는 헤더만 들어간다`() {
        val info = service.generatePresignedUploadUrl(1L, "image/png")

        // 앱은 Content-Type만 보낸다. 다른 헤더를 서명하면 업로드가 SignatureDoesNotMatch로 실패한다
        assertTrue(info.url.contains("X-Amz-SignedHeaders=content-type%3Bhost"), info.url.substringAfter('?'))
    }

    @Test
    fun `확정한 사진은 같은 키에 5분 캐시 설정을 붙이고 형식을 유지한다`() {
        service.applyProfileImageCachePolicy("profiles/1/a.png")

        val captor = argumentCaptor<CopyObjectRequest>()
        verify(s3Client).copyObject(captor.capture())
        val request = captor.firstValue
        assertEquals("profiles/1/a.png", request.sourceKey())
        assertEquals("profiles/1/a.png", request.destinationKey())
        assertEquals(MetadataDirective.REPLACE, request.metadataDirective())
        assertEquals("image/png", request.contentType())
        assertEquals("public, max-age=300", request.cacheControl())
    }
}
