package com.runnershi.domain.user.controller

import com.runnershi.domain.user.service.UserService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "User", description = "유저 API")
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
)
