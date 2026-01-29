package com.runnershi.domain.terms.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "terms_agreements",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "terms_id"])
    ],
    indexes = [
        Index(name = "idx_terms_agreements_user_id", columnList = "user_id")
    ]
)
class TermsAgreement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id", nullable = false)
    val terms: Terms,

    @Column(nullable = false)
    val agreedVersion: Int,

    @Column(nullable = false)
    val agreedAt: LocalDateTime = LocalDateTime.now()
)
