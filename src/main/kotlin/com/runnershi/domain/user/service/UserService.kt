package com.runnershi.domain.user.service

import com.runnershi.auth.client.GoogleClient
import com.runnershi.auth.client.KakaoClient
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.repository.RegionRepository
import com.runnershi.domain.user.dto.MyProfileResponse
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val regionRepository: RegionRepository,
    private val missionChecker: MissionChecker,
    private val googleClient: GoogleClient,
    private val kakaoClient: KakaoClient
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun updateNickname(userId: Long, nickname: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (userRepository.existsByNickname(nickname)) {
            throw BusinessException(ErrorCode.DUPLICATE_NICKNAME)
        }

        user.nickname = nickname
    }

    @Transactional(readOnly = true)
    fun checkNicknameAvailable(nickname: String): Boolean {
        return !userRepository.existsByNickname(nickname)
    }

    @Transactional(readOnly = true)
    fun getMyProfile(userId: Long): MyProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        val region = user.regionId?.let { regionId ->
            regionRepository.findById(regionId)
                .map { RegionResponse.from(it) }
                .orElse(null)
        }

        return MyProfileResponse.from(user, region)
    }

    @Transactional
    fun updateRegion(userId: Long, regionId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 지역 존재 여부 확인
        if (!regionRepository.existsById(regionId)) {
            throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 지역입니다")
        }

        user.regionId = regionId

        // 미션 달성 체크 (REGION_SET)
        missionChecker.checkOnRegionSet(userId)
    }

    @Transactional
    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // OAuth 연결 해제 (실패해도 탈퇴 진행)
        revokeOAuthConnection(user.provider, user.providerId)

        user.status = UserStatus.WITHDRAWN
        user.deletedAt = LocalDateTime.now()
        user.refreshToken = null
        user.refreshTokenExpiresAt = null
    }

    private fun revokeOAuthConnection(provider: Provider, providerId: String) {
        try {
            when (provider) {
                Provider.GOOGLE -> googleClient.revokeToken(providerId)
                Provider.KAKAO -> kakaoClient.unlinkUser(providerId)
                Provider.APPLE -> log.info("Apple revoke는 클라이언트에서 처리 (providerId={})", providerId)
            }
        } catch (e: Exception) {
            log.warn("OAuth revoke 실패 (탈퇴는 계속 진행): provider={}, error={}", provider, e.message)
        }
    }
}
