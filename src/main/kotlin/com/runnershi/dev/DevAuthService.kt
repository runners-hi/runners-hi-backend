package com.runnershi.dev

import com.runnershi.common.security.JwtTokenProvider
import com.runnershi.common.util.NicknameGenerator
import com.runnershi.domain.auth.dto.AuthResponse
import com.runnershi.domain.region.repository.DistrictRepository
import com.runnershi.domain.region.repository.RegionRepository
import com.runnershi.domain.terms.entity.TermsAgreement
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.terms.repository.TermsRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.User
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Profile("dev")
class DevAuthService(
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val nicknameGenerator: NicknameGenerator,
    private val regionRepository: RegionRepository,
    private val districtRepository: DistrictRepository,
    private val termsRepository: TermsRepository,
    private val termsAgreementRepository: TermsAgreementRepository,
    private val devDataSeeder: DevDataSeeder
) {

    @Transactional
    fun login(userKey: String, onboarded: Boolean): AuthResponse {
        val providerId = "dev:$userKey"
        val existing = userRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
        val isNew = existing == null
        val user = existing ?: createUser(providerId, onboarded)
        user.lastLoginAt = LocalDateTime.now()

        val accessToken = jwtTokenProvider.createAccessToken(user.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)
        user.refreshToken = refreshToken
        user.refreshTokenExpiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenValidity() / 1000)

        // 온보딩을 테스트하는 새 회원(onboarded=false)에게는 기록을 넣지 않는다
        if (user.status == UserStatus.ACTIVE) devDataSeeder.seedIfEmpty(user)

        return AuthResponse(accessToken = accessToken, refreshToken = refreshToken, isNewUser = isNew)
    }

    private fun createUser(providerId: String, onboarded: Boolean): User {
        val user = userRepository.save(
            User(
                provider = Provider.KAKAO,
                providerId = providerId,
                nickname = nicknameGenerator.generateUnique { userRepository.existsByNickname(it) },
                status = if (onboarded) UserStatus.ACTIVE else UserStatus.PENDING
            )
        )
        if (onboarded) {
            // 서울 강남구: 시드 회원 8명이 있는 지역이라 랭킹이 바로 채워진다
            regionRepository.findAll().firstOrNull { it.name == "서울특별시" }?.let { region ->
                user.regionId = region.id
                user.districtId = districtRepository.findAllByRegionIdOrderByIsDefaultAscNameAsc(region.id)
                    .firstOrNull { it.name == "강남구" }?.id
                    ?: districtRepository.findFirstByRegionIdAndIsDefaultTrue(region.id)?.id
            }
            val now = LocalDateTime.now()
            termsAgreementRepository.saveAll(
                termsRepository.findAll().map { TermsAgreement(userId = user.id, terms = it, agreedVersion = it.version, agreedAt = now) }
            )
        }
        return user
    }
}
