package com.runnershi.domain.user.repository

import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByProviderAndProviderId(provider: Provider, providerId: String): User?
    fun existsByNickname(nickname: String): Boolean
}
