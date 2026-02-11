package com.runnershi.domain.terms.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.terms.dto.TermsAgreementItem
import com.runnershi.domain.terms.dto.TermsAgreementRequest
import com.runnershi.domain.terms.entity.ContentType
import com.runnershi.domain.terms.entity.Terms
import com.runnershi.domain.terms.entity.TermsAgreement
import com.runnershi.domain.terms.entity.TermsCode
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.terms.repository.TermsRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TermsServiceTest {

    @Mock lateinit var termsRepository: TermsRepository
    @Mock lateinit var termsAgreementRepository: TermsAgreementRepository
    @Mock lateinit var userRepository: UserRepository

    private lateinit var termsService: TermsService

    @BeforeEach
    fun setUp() {
        termsService = TermsService(termsRepository, termsAgreementRepository, userRepository)
    }

    private fun createTerms(id: Long, code: TermsCode, title: String, required: Boolean, displayOrder: Int): Terms {
        val terms = Terms(code = code, title = title, required = required, displayOrder = displayOrder)
        ReflectionTestUtils.setField(terms, "id", id)
        return terms
    }

    private fun createUser(id: Long = 1L, status: UserStatus = UserStatus.PENDING): User {
        val user = User(
            provider = Provider.KAKAO,
            providerId = "kakao-123",
            nickname = "테스트러너",
            status = status
        )
        ReflectionTestUtils.setField(user, "id", id)
        return user
    }

    @Nested
    @DisplayName("약관 목록 조회")
    inner class GetTermsList {

        private val termsList = listOf(
            createTerms(1L, TermsCode.TERMS_OF_SERVICE, "서비스 이용약관", true, 1),
            createTerms(2L, TermsCode.PRIVACY_POLICY, "개인정보 처리방침", true, 2),
            createTerms(3L, TermsCode.MARKETING, "마케팅 수신 동의", false, 3)
        )

        @Test
        @DisplayName("비로그인 유저 - 동의 여부 없이 목록 반환")
        fun withoutLogin() {
            whenever(termsRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(termsList)

            val result = termsService.getTermsList(null)

            assertEquals(3, result.size)
            assertFalse(result[0].agreed)
            assertFalse(result[1].agreed)
            assertFalse(result[2].agreed)
        }

        @Test
        @DisplayName("로그인 유저 - 동의 여부 포함하여 목록 반환")
        fun withLogin() {
            whenever(termsRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(termsList)
            val agreement = TermsAgreement(userId = 1L, terms = termsList[0], agreedVersion = 1)
            whenever(termsAgreementRepository.findAllByUserId(1L)).thenReturn(listOf(agreement))

            val result = termsService.getTermsList(1L)

            assertEquals(3, result.size)
            assertTrue(result[0].agreed)
            assertFalse(result[1].agreed)
            assertFalse(result[2].agreed)
        }
    }

    @Nested
    @DisplayName("약관 동의")
    inner class AgreeToTerms {

        private val requiredTerms1 = createTerms(1L, TermsCode.TERMS_OF_SERVICE, "서비스 이용약관", true, 1)
        private val requiredTerms2 = createTerms(2L, TermsCode.PRIVACY_POLICY, "개인정보 처리방침", true, 2)
        private val optionalTerms = createTerms(3L, TermsCode.MARKETING, "마케팅 수신 동의", false, 3)
        private val allTerms = listOf(requiredTerms1, requiredTerms2, optionalTerms)

        @Test
        @DisplayName("필수 약관 전체 동의 시 성공 및 상태 ACTIVE 변경")
        fun success_pendingToActive() {
            val user = createUser(status = UserStatus.PENDING)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(termsRepository.findAllById(listOf(1L, 2L, 3L)))
                .thenReturn(allTerms)
            whenever(termsRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(allTerms)

            val request = TermsAgreementRequest(listOf(
                TermsAgreementItem(1L, true),
                TermsAgreementItem(2L, true),
                TermsAgreementItem(3L, true)
            ))

            termsService.agreeToTerms(1L, request)

            assertEquals(UserStatus.ACTIVE, user.status)
            verify(termsAgreementRepository).deleteAllByUserId(1L)
            verify(termsAgreementRepository).saveAll(any<List<TermsAgreement>>())
        }

        @Test
        @DisplayName("이미 ACTIVE인 유저는 상태 유지")
        fun alreadyActive_staysActive() {
            val user = createUser(status = UserStatus.ACTIVE)
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(termsRepository.findAllById(listOf(1L, 2L)))
                .thenReturn(listOf(requiredTerms1, requiredTerms2))
            whenever(termsRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(allTerms)

            val request = TermsAgreementRequest(listOf(
                TermsAgreementItem(1L, true),
                TermsAgreementItem(2L, true)
            ))

            termsService.agreeToTerms(1L, request)

            assertEquals(UserStatus.ACTIVE, user.status)
        }

        @Test
        @DisplayName("필수 약관 미동의 시 예외 발생")
        fun missingRequired_throwsException() {
            val user = createUser()
            whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))
            whenever(termsRepository.findAllById(listOf(1L)))
                .thenReturn(listOf(requiredTerms1))
            whenever(termsRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(allTerms)

            val request = TermsAgreementRequest(listOf(
                TermsAgreementItem(1L, true)
                // requiredTerms2 누락
            ))

            val exception = assertThrows<BusinessException> {
                termsService.agreeToTerms(1L, request)
            }
            assertEquals(ErrorCode.INVALID_INPUT, exception.errorCode)
        }

        @Test
        @DisplayName("존재하지 않는 유저 - 예외 발생")
        fun userNotFound() {
            whenever(userRepository.findById(999L)).thenReturn(Optional.empty())

            val request = TermsAgreementRequest(listOf(TermsAgreementItem(1L, true)))

            val exception = assertThrows<BusinessException> {
                termsService.agreeToTerms(999L, request)
            }
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.errorCode)
        }
    }
}
