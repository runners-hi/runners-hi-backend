package com.runnershi.domain.terms.dto

import com.runnershi.domain.terms.entity.ContentType
import com.runnershi.domain.terms.entity.Terms
import com.runnershi.domain.terms.entity.TermsCode

data class TermsResponse(
    val id: Long,
    val code: TermsCode,
    val title: String,
    val contentType: ContentType,
    val contentUrl: String?,
    val required: Boolean,
    val version: Int,
    val agreed: Boolean = false
) {
    companion object {
        fun from(terms: Terms, agreed: Boolean = false) = TermsResponse(
            id = terms.id,
            code = terms.code,
            title = terms.title,
            contentType = terms.contentType,
            contentUrl = terms.contentUrl,
            required = terms.required,
            version = terms.version,
            agreed = agreed
        )
    }
}

data class TermsAgreementRequest(
    val agreements: List<TermsAgreementItem>
)

data class TermsAgreementItem(
    val termsId: Long,
    val agreed: Boolean
)
