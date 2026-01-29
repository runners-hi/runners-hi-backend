package com.runnershi.domain.user.service

import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
)
