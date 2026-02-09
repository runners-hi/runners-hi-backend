package com.runnershi.domain.level.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "level_config")
class LevelConfig(
    @Id
    val level: Int,

    @Column(name = "required_experience", nullable = false)
    val requiredExperience: Int
)
