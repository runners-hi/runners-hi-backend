package com.runnershi.auth.client

import com.runnershi.domain.user.entity.Provider
import org.springframework.stereotype.Component

@Component
class AppleClient : AuthClient {

    override fun getProvider(): Provider = Provider.APPLE

    override fun verify(token: String): UserInfo {
        // TODO: Apple id_token 검증 구현
        // 1. Apple 공개키로 JWT 검증 (https://appleid.apple.com/auth/keys)
        // 2. sub claim에서 providerId 추출
        // 3. email은 최초 로그인 때만 제공됨 주의
        throw NotImplementedError("Apple 로그인 미구현")
    }
}
