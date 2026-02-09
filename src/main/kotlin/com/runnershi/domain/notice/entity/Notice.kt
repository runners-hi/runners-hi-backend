package com.runnershi.domain.notice.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "notices")
class Notice(
    @Column(nullable = false, length = 200)
    val title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseEntity()
