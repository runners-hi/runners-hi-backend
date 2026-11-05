package com.runnershi.common.storage

import org.springframework.stereotype.Component

/** DB에는 object key를 저장하고, 응답에는 앱이 바로 열 수 있는 주소를 내려준다. */
@Component
class ProfileImageUrlResolver(properties: StorageProperties) {

    private val baseUrl = properties.publicBaseUrl.trimEnd('/')

    fun resolve(value: String?): String? = when {
        value.isNullOrBlank() -> null
        value.startsWith("http://") || value.startsWith("https://") -> value
        else -> "$baseUrl/$value"
    }
}
