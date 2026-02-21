package com.runnershi.domain.user.entity

enum class UserStatus {
    PENDING,    // 가입 진행 중 (약관 동의 전)
    ACTIVE,     // 활성 사용자
    SUSPENDED,  // 정지된 사용자
    WITHDRAWN,  // 탈퇴한 사용자 (30일 유예 기간)
    DELETED     // 30일 경과 후 개인정보 삭제 완료
}
