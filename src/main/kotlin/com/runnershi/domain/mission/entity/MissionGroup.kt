package com.runnershi.domain.mission.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDate

// === 미션 그룹 (이벤트 단위) ===
// - WELCOME: 상시 미션 (startDate/endDate null)
// - EVENT: 기간 한정 이벤트 (추석, 할로윈, Holiday Run 등)
// - showOnHome: 홈 화면 "미션 이벤트" 섹션 노출 여부
// - 운영팀이 DB에서 직접 관리 (추후 어드민 API 구현 예정)
@Entity
@Table(name = "mission_groups")
class MissionGroup(
    @Column(nullable = false, length = 100)
    val name: String,

    @Column(length = 500)
    val description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: MissionGroupType,

    @Column(name = "start_date")
    val startDate: LocalDate? = null,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,

    @Column(name = "show_on_home", nullable = false)
    val showOnHome: Boolean = false
) : BaseEntity()
