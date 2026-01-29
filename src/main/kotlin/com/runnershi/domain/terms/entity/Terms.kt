package com.runnershi.domain.terms.entity

import com.runnershi.common.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "terms")
class Terms(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    val code: TermsCode,

    @Column(nullable = false)
    val title: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val contentType: ContentType = ContentType.NONE,

    val contentUrl: String? = null,

    @Column(nullable = false)
    val required: Boolean,

    @Column(nullable = false)
    var version: Int = 1,

    @Column(nullable = false)
    val displayOrder: Int
) : BaseEntity()

enum class TermsCode {
    TERMS_OF_SERVICE,      // 서비스 이용약관
    PRIVACY_POLICY,        // 개인정보 수집 및 이용
    MARKETING,             // 마케팅 정보 수신
    SNS,                   // SNS 수신
    AGE_VERIFICATION       // 만 14세 이상
}

enum class ContentType {
    NONE,      // 내용 없음 (체크만)
    IN_APP,    // 앱 내 화면
    WEB_URL    // 외부 웹 URL
}
