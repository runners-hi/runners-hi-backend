package com.runnershi.domain.terms.repository

import com.runnershi.domain.terms.entity.TermsAgreement
import org.springframework.data.jpa.repository.JpaRepository

interface TermsAgreementRepository : JpaRepository<TermsAgreement, Long> {
    fun findAllByUserId(userId: Long): List<TermsAgreement>
    fun existsByUserIdAndTermsId(userId: Long, termsId: Long): Boolean
    fun deleteAllByUserId(userId: Long)
}
