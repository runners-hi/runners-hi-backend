package com.runnershi.domain.region.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "districts",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["region_id", "name"])
    ],
    indexes = [
        Index(name = "idx_districts_region_name", columnList = "region_id, name")
    ]
)
class District(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "region_id", nullable = false)
    val regionId: Long,

    @Column(nullable = false)
    val name: String,

    @Column(name = "is_default", nullable = false)
    val isDefault: Boolean = false
)
