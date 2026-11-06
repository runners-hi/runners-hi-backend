package com.runnershi.domain.terms

import com.runnershi.domain.terms.dto.TermsResponse
import com.runnershi.domain.terms.entity.ContentType
import com.runnershi.domain.terms.entity.Terms
import com.runnershi.domain.terms.entity.TermsCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TermsContentFilesTest {

    private fun text(code: TermsCode): String =
        javaClass.getResource("/terms/${code.name}.txt")!!.readText()

    @Test
    @DisplayName("약관 7종 본문 파일이 모두 있고 비어 있지 않다")
    fun allSevenExist() {
        assertEquals(7, TermsCode.entries.size)
        TermsCode.entries.forEach { assertTrue(text(it).length > 100, "${it.name} 본문이 너무 짧음") }
    }

    @Test
    @DisplayName("개인정보 처리방침에 필수 항목이 들어 있다")
    fun privacyHasRequiredItems() {
        val t = text(TermsCode.PRIVACY_POLICY)
        listOf("3개월", "카카오", "구글", "애플", "랭킹", "Health Connect", "HealthKit", "탈퇴", "2026년 11월", "Google Analytics for Firebase", "14개월", "광고 식별자").forEach {
            assertTrue(t.contains(it), "개인정보 처리방침에 '$it' 없음")
        }
    }

    @Test
    @DisplayName("응답에 content와 version이 담긴다")
    fun responseCarriesContentAndVersion() {
        val terms = Terms(
            code = TermsCode.LOCATION, title = "위치", contentType = ContentType.IN_APP,
            required = true, version = 2, content = "본문", displayOrder = 1
        )
        val r = TermsResponse.from(terms)
        assertEquals("본문", r.content)
        assertEquals(2, r.version)
    }

    @Test
    @DisplayName("본문이 없으면 빈 문자열이다")
    fun nullContentBecomesEmpty() {
        val terms = Terms(code = TermsCode.SNS, title = "SNS", contentType = ContentType.NONE, required = false, displayOrder = 2)
        assertEquals("", TermsResponse.from(terms).content)
    }
}
