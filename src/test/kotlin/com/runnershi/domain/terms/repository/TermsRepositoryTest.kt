package com.runnershi.domain.terms.repository

import com.runnershi.domain.terms.entity.ContentType
import com.runnershi.domain.terms.entity.Terms
import com.runnershi.domain.terms.entity.TermsAgreement
import com.runnershi.domain.terms.entity.TermsCode
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.repository.UserRepository
import com.runnershi.support.RepositoryTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TermsRepositoryTest : RepositoryTest() {

    @Autowired lateinit var termsRepository: TermsRepository
    @Autowired lateinit var termsAgreementRepository: TermsAgreementRepository
    @Autowired lateinit var userRepository: UserRepository

    private lateinit var terms1: Terms
    private lateinit var terms2: Terms
    private lateinit var terms3: Terms

    @BeforeEach
    fun setUp() {
        terms1 = termsRepository.save(Terms(code = TermsCode.TERMS_OF_SERVICE, title = "서비스 이용약관", contentType = ContentType.WEB_URL, contentUrl = "https://example.com", required = true, displayOrder = 1))
        terms2 = termsRepository.save(Terms(code = TermsCode.PRIVACY_POLICY, title = "개인정보 처리방침", required = true, displayOrder = 2))
        terms3 = termsRepository.save(Terms(code = TermsCode.MARKETING, title = "마케팅 수신 동의", required = false, displayOrder = 3))
    }

    @Nested
    @DisplayName("TermsRepository")
    inner class TermsRepo {

        @Test
        @DisplayName("displayOrder 순서로 조회")
        fun findAllByOrderByDisplayOrderAsc() {
            val result = termsRepository.findAllByOrderByDisplayOrderAsc()

            assertEquals(3, result.size)
            assertEquals("서비스 이용약관", result[0].title)
            assertEquals("개인정보 처리방침", result[1].title)
            assertEquals("마케팅 수신 동의", result[2].title)
        }

        @Test
        @DisplayName("코드로 약관 조회")
        fun findByCode() {
            val result = termsRepository.findByCode(TermsCode.TERMS_OF_SERVICE)

            assertNotNull(result)
            assertEquals("서비스 이용약관", result.title)
        }
    }

    @Nested
    @DisplayName("TermsAgreementRepository")
    inner class TermsAgreementRepo {

        private lateinit var user: User

        @BeforeEach
        fun setUpUser() {
            user = userRepository.save(User(
                provider = Provider.KAKAO,
                providerId = "kakao-123",
                nickname = "테스트러너"
            ))
        }

        @Test
        @DisplayName("유저의 동의 목록 조회")
        fun findAllByUserId() {
            termsAgreementRepository.save(TermsAgreement(userId = user.id, terms = terms1, agreedVersion = 1))
            termsAgreementRepository.save(TermsAgreement(userId = user.id, terms = terms2, agreedVersion = 1))

            val result = termsAgreementRepository.findAllByUserId(user.id)

            assertEquals(2, result.size)
        }

        @Test
        @DisplayName("유저의 특정 약관 동의 여부 확인")
        fun existsByUserIdAndTermsId() {
            termsAgreementRepository.save(TermsAgreement(userId = user.id, terms = terms1, agreedVersion = 1))

            assertTrue(termsAgreementRepository.existsByUserIdAndTermsId(user.id, terms1.id))
            assertFalse(termsAgreementRepository.existsByUserIdAndTermsId(user.id, terms3.id))
        }

        @Test
        @DisplayName("유저의 모든 동의 삭제")
        fun deleteAllByUserId() {
            termsAgreementRepository.save(TermsAgreement(userId = user.id, terms = terms1, agreedVersion = 1))
            termsAgreementRepository.save(TermsAgreement(userId = user.id, terms = terms2, agreedVersion = 1))

            termsAgreementRepository.deleteAllByUserId(user.id)

            val result = termsAgreementRepository.findAllByUserId(user.id)
            assertEquals(0, result.size)
        }
    }
}
