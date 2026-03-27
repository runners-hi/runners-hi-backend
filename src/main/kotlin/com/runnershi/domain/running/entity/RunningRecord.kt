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
// - heartRateAvg/Max: bpm (선택)
// - elevationGain/Loss: m (선택)
// - runningType: 러닝 유형 (선택)

// === 정책 ===
// - pace는 클라이언트에서 받지 않고 서버에서 계산
// - runningDate는 startedAt 기준으로 서버에서 설정
// - 유저 탈퇴(soft delete) 시 러닝 기록은 유지
// - 기록 생성 시 User.totalDistance 동기화 필요
// - GPS 경로: 위치사업자 등록 필요로 제외
// - 케이던스/날씨: MVP 범위 외 제외

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

    val memo: String? = null,

    // 심박수 (bpm, 선택)
    @Column(name = "heart_rate_avg")
    val heartRateAvg: Int? = null,

    @Column(name = "heart_rate_max")
    val heartRateMax: Int? = null,

    // 고도 (m, 선택)
    @Column(name = "elevation_gain")
    val elevationGain: Int? = null,

    @Column(name = "elevation_loss")
    val elevationLoss: Int? = null,

    // 러닝 유형 (선택)
    @Enumerated(EnumType.STRING)
    @Column(name = "running_type", length = 20)
    val runningType: RunningType? = null
) : BaseEntity()
