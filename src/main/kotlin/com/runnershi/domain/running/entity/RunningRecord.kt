package com.runnershi.domain.running.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

// === 단위 기준 ===
// - distance: m (Int) - 미터 단위 정수 저장, 표시 시 / 1000.0 으로 km 변환
// - duration: 초 (Int)
// - pace: 초/km (Int) - 서버에서 distance/duration으로 계산
// - runningDate: startedAt에서 서버가 추출

// === 정책 ===
// - pace는 클라이언트에서 받지 않고 서버에서 계산
// - runningDate는 startedAt 기준으로 서버에서 설정
// - source가 없으면 MANUAL로 처리
// - 유저 탈퇴(soft delete) 시 러닝 기록은 유지
// - 기록 생성 시 User.totalDistance 동기화 필요

// 경로, 심박수, 케이던스, 고도, 날씨, 러닝 타입은 화면에 쓰지 않아 저장하지 않는다 (BE-094)

@Entity
@Table(
    name = "running_records",
    indexes = [
        Index(name = "idx_running_records_user_date", columnList = "user_id, running_date"),
        Index(name = "idx_running_records_user_created", columnList = "user_id, created_at")
    ]
)
class RunningRecord(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    // startedAt 기준으로 서버에서 설정
    @Column(name = "running_date", nullable = false)
    val runningDate: LocalDate,

    // 단위: m (미터)
    @Column(nullable = false)
    val distance: Int,

    // 단위: 초
    @Column(nullable = false)
    val duration: Int,

    // 단위: 초/km, 서버에서 계산
    @Column(nullable = false)
    val pace: Int,

    val calories: Int? = null,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime,

    @Column(name = "ended_at", nullable = false)
    val endedAt: LocalDateTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val source: RunningRecordSource = RunningRecordSource.MANUAL,

    val memo: String? = null
) : BaseEntity()
