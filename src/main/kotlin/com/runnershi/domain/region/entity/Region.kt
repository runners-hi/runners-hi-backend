package com.runnershi.domain.region.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "regions")
class Region(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: RegionType
)

enum class RegionType {
    SPECIAL_CITY,           // 특별시 (서울)
    METROPOLITAN_CITY,      // 광역시 (부산, 대구, 인천, 광주, 대전, 울산)
    SPECIAL_SELF_GOVERNING_CITY,  // 특별자치시 (세종)
    SPECIAL_SELF_GOVERNING_PROVINCE,  // 특별자치도 (제주)
    CITY                    // 일반 시
}
