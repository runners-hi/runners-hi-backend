package com.runnershi.domain.mission.entity

// === 미션 달성 조건 타입 ===
// - 새 타입 추가 시 enum 추가 + MissionChecker에 체크 로직 구현 필요
// - conditionType은 체크 로직과 1:1 매핑되므로 DB 테이블이 아닌 enum으로 관리
enum class ConditionType {
    SINGLE_DISTANCE,       // 1회 러닝 거리 달성 (conditionValue: m)
    CUMULATIVE_DISTANCE,   // 누적 거리 달성 (conditionValue: m)
    PACE,                  // 페이스 달성 (conditionValue: 초/km 이하)
    RUN_ON_DATE,           // 특정 날짜에 러닝 (conditionStartDate 사용)
    RUN_IN_PERIOD,         // 특정 기간 내 러닝 (conditionStartDate ~ conditionEndDate)
    FIRST_RUN,             // 첫 러닝 등록
    REGION_SET             // 지역 인증
}
