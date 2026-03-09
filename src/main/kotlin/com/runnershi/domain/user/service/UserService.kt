package com.runnershi.domain.user.service

import com.runnershi.auth.client.AppleTokenClient
import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.level.repository.UserLevelSnapshotRepository
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.mission.repository.UserMissionRepository
import com.runnershi.domain.region.dto.DistrictResponse
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.repository.DistrictRepository
import com.runnershi.domain.region.repository.RegionRepository
import com.runnershi.domain.terms.repository.TermsAgreementRepository
import com.runnershi.domain.user.entity.Provider
import com.runnershi.domain.user.dto.MyProfileResponse
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
    private val districtRepository: DistrictRepository,
    private val missionChecker: MissionChecker,
    private val termsAgreementRepository: TermsAgreementRepository,
    private val userMissionRepository: UserMissionRepository,
    private val userLevelSnapshotRepository: UserLevelSnapshotRepository,
    private val appleTokenClient: AppleTokenClient
) {
    companion object {
        private val log = LoggerFactory.getLogger(UserService::class.java)
    }

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
        val district = user.districtId?.let { districtId ->
            districtRepository.findById(districtId)
                .map { DistrictResponse.from(it) }
                .orElse(null)
        }

        return MyProfileResponse.from(user, region, district)
    }

    @Transactional
    fun updateRegion(userId: Long, regionId: Long, districtId: Long?) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        if (!regionRepository.existsById(regionId)) {
            throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 지역입니다")
        }

        val district = districtId?.let {
            districtRepository.findByIdAndRegionId(it, regionId)
                ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "존재하지 않는 세부 지역입니다")
        } ?: districtRepository.findFirstByRegionIdAndIsDefaultTrue(regionId)
            ?: throw BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "기본 세부 지역이 존재하지 않습니다")

        user.regionId = regionId
        user.districtId = district.id

        // 미션 달성 체크 (REGION_SET)
        missionChecker.checkOnRegionSet(userId)
    }

    @Transactional
    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        revokeAppleAccountIfPossible(user.provider, user.appleRefreshToken)

        termsAgreementRepository.deleteAllByUserId(userId)
        userMissionRepository.deleteAllByUserId(userId)
        userLevelSnapshotRepository.deleteAllByUserId(userId)

        user.providerId = "withdrawn_${user.id}"
        user.email = null
        user.nickname = "withdrawn_${user.id}"
        user.profileImageUrl = null
        user.regionId = null
        user.districtId = null
        user.notificationEnabled = false
        user.fcmToken = null
        user.appleRefreshToken = null
        user.status = UserStatus.WITHDRAWN
        user.deletedAt = LocalDateTime.now()
        user.refreshToken = null
        user.refreshTokenExpiresAt = null
    }

    private fun revokeAppleAccountIfPossible(provider: Provider, appleRefreshToken: String?) {
        if (provider != Provider.APPLE) return
        if (appleRefreshToken.isNullOrBlank()) {
            log.warn("Apple withdrawal skipped revoke because refresh token is missing")
            return
        }

        appleTokenClient.revokeRefreshToken(appleRefreshToken)
    }
}
