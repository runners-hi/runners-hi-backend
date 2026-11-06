package com.runnershi.domain.auth.service

import com.runnershi.domain.user.entity.User

/** 로그인에 성공한 뒤 불리는 훅. 상용에는 구현체가 없고 개발(dev) 프로필에만 있다. */
fun interface LoginHook {
    fun onLogin(user: User)
}
