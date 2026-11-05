package com.runnershi.common.storage

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ProfileImageUrlResolverTest {

    @Test
    fun `key는 버킷 공개 주소로 바꾼다`() {
        val resolver = ProfileImageUrlResolver(GcsProperties("bucket", "project"))
        assertEquals(
            "https://storage.googleapis.com/bucket/profiles/1/a.jpg",
            resolver.resolve("profiles/1/a.jpg")
        )
    }

    @Test
    fun `공개 주소 설정이 있으면 그 주소를 쓴다`() {
        val resolver = ProfileImageUrlResolver(GcsProperties("bucket", "project", "https://cdn.example.com/"))
        assertEquals("https://cdn.example.com/profiles/1/a.jpg", resolver.resolve("profiles/1/a.jpg"))
    }

    @Test
    fun `이미 주소이거나 값이 없으면 그대로 둔다`() {
        val resolver = ProfileImageUrlResolver(GcsProperties("bucket", "project"))
        assertEquals("https://x.com/a.jpg", resolver.resolve("https://x.com/a.jpg"))
        assertNull(resolver.resolve(null))
    }
}
