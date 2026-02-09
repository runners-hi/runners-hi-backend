package com.runnershi.domain.mission.entity

enum class MissionStatus {
    NOT_ACHIEVED,   // 미달성
    IN_PROGRESS,    // 진행 중 (누적 조건 등에서 일부 진행)
    ACHIEVED        // 달성 완료
}
