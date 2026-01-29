package com.runnershi.domain.terms.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.terms.dto.TermsAgreementRequest
import com.runnershi.domain.terms.dto.TermsResponse
import com.runnershi.domain.terms.entity.TermsAgreement
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.terms.repository.TermsRepository
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TermsService(
    private val termsRepository: TermsRepository,
    private val termsAgreementRepository: TermsAgreementRepository,
    private val userRepository: UserRepository
) {

    @Transactional(readOnly = true)
    fun getTermsList(userId: Long?): List<TermsResponse> {
        val termsList = termsRepository.findAllByOrderByDisplayOrderAsc()

        val agreedTermsIds = userId?.let {
            termsAgreementRepository.findAllByUserId(it)
                .map { agreement -> agreement.terms.id }
                .toSet()
        } ?: emptySet()

        return termsList.map { terms ->
            TermsResponse.from(terms, agreedTermsIds.contains(terms.id))
        }
    }

    @Transactional
    fun agreeToTerms(userId: Long, request: TermsAgreementRequest) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val allTerms = termsRepository.findAllById(
            request.agreements.map { it.termsId }
        ).associateBy { it.id }

        // 필수 약관 동의 여부 확인
        val requiredTerms = termsRepository.findAllByOrderByDisplayOrderAsc()
            .filter { it.required }

        val agreedTermsIds = request.agreements
            .filter { it.agreed }
            .map { it.termsId }
            .toSet()

        val missingRequired = requiredTerms.filter { it.id !in agreedTermsIds }
        if (missingRequired.isNotEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT, "필수 약관에 동의해야 합니다: ${missingRequired.map { it.title }}")
        }

        // 기존 동의 삭제 후 새로 저장
        termsAgreementRepository.deleteAllByUserId(userId)

        val now = LocalDateTime.now()
        val agreements = request.agreements
            .filter { it.agreed }
            .mapNotNull { item ->
                allTerms[item.termsId]?.let { terms ->
                    TermsAgreement(
                        userId = userId,
                        terms = terms,
                        agreedVersion = terms.version,
                        agreedAt = now
                    )
                }
            }

        termsAgreementRepository.saveAll(agreements)

        // 약관 동의 완료 시 상태 변경
        if (user.status == UserStatus.PENDING) {
            user.status = UserStatus.ACTIVE
        }
    }
}
