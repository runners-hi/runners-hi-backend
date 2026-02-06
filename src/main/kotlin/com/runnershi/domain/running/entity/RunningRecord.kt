package com.runnershi.domain.running.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

// TODO: 추가 고려 필드
// 1. 경로 데이터 - GPS 좌표 (별도 테이블 or JSON) - 위치사업자 등록 여부 확인 필요
// 2. 심박수 - 평균/최대 심박수
// 3. 케이던스 - 분당 걸음 수
// 4. 고도 - 상승/하강 고도
// 5. 날씨 - 기온, 날씨 상태
// 6. 러닝 타입 - 일반/인터벌/레이스 등

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

    @Column(name = "running_date", nullable = false)
    val runningDate: LocalDate,

    @Column(nullable = false)
    val distance: Double,

    @Column(nullable = false)
    val duration: Int,

    @Column(nullable = false)
    val pace: Int,

    val calories: Int? = null,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime,

    @Column(name = "ended_at", nullable = false)
    val endedAt: LocalDateTime,

    val memo: String? = null
) : BaseEntity()
