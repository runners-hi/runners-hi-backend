package com.runnershi.auth.client

import com.runnershi.domain.user.entity.Provider
import org.springframework.stereotype.Component

@Component
class GoogleClient : AuthClient {

    override fun getProvider(): Provider = Provider.GOOGLE

    override fun verify(token: String): UserInfo {
        // TODO: Google id_token 검증 구현
        // 1. Google 공개키로 JWT 검증
        // 2. sub claim에서 providerId 추출
        // 3. email, name claim 추출
        throw NotImplementedError("Google 로그인 미구현")
    }
}
