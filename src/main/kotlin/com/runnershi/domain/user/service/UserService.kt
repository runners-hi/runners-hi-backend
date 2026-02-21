package com.runnershi.domain.user.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.common.storage.PresignedUrlInfo
import com.runnershi.common.storage.StorageService
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.region.dto.RegionResponse
import com.runnershi.domain.region.repository.RegionRepository
import com.runnershi.domain.user.dto.MyProfileResponse
import com.runnershi.domain.user.dto.ProfileImageUploadResponse
import com.runnershi.domain.user.entity.UserStatus
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UserService(
    private val userRepository: UserRepository,
    private val regionRepository: RegionRepository,
    private val missionChecker: MissionChecker,
    private val storageService: StorageService
) {

    companion object {
        private val ALLOWED_IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")
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

    fun generateProfileImageUploadUrl(userId: Long, contentType: String): ProfileImageUploadResponse {
        if (contentType !in ALLOWED_IMAGE_TYPES) {
            throw BusinessException(ErrorCode.INVALID_IMAGE_TYPE)
        }

        val presignedUrlInfo = storageService.generatePresignedUploadUrl(userId, contentType)
        return ProfileImageUploadResponse(
            presignedUrl = presignedUrlInfo.url,
            objectKey = presignedUrlInfo.objectKey
        )
    }

    @Transactional
    fun confirmProfileImage(userId: Long, objectKey: String) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // 이전 이미지가 있으면 GCS에서 삭제
        user.profileImageUrl?.let { storageService.deleteObject(it) }

        user.profileImageUrl = objectKey
    }

    @Transactional
    fun removeProfileImage(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.profileImageUrl?.let { storageService.deleteObject(it) }
        user.profileImageUrl = null
    }

    @Transactional
    fun withdraw(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        user.status = UserStatus.WITHDRAWN
        user.deletedAt = LocalDateTime.now()
        user.refreshToken = null
        user.refreshTokenExpiresAt = null
    }
}
