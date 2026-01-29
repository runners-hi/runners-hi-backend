package com.runnershi.domain.terms.repository

import com.runnershi.domain.terms.entity.Terms
import com.runnershi.domain.terms.entity.TermsCode
import org.springframework.data.jpa.repository.JpaRepository

interface TermsRepository : JpaRepository<Terms, Long> {
    fun findAllByOrderByDisplayOrderAsc(): List<Terms>
    fun findByCode(code: TermsCode): Terms?
}
