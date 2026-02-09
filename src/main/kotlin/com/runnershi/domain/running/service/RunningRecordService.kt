package com.runnershi.domain.running.service

import com.runnershi.common.exception.BusinessException
import com.runnershi.common.exception.ErrorCode
import com.runnershi.domain.level.service.LevelService
import com.runnershi.domain.mission.service.MissionChecker
import com.runnershi.domain.running.dto.DailyRecordResponse
import com.runnershi.domain.running.dto.RunningRecordCreateRequest
import com.runnershi.domain.running.dto.RunningRecordListResponse
import com.runnershi.domain.running.dto.RunningRecordResponse
import com.runnershi.domain.running.dto.WeeklySummaryResponse
import com.runnershi.domain.running.entity.RunningRecord
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@Service
class RunningRecordService(
    private val runningRecordRepository: RunningRecordRepository,
    private val userRepository: UserRepository,
    private val levelService: LevelService,
    private val missionChecker: MissionChecker
) {

    // === 러닝 기록 저장 ===
    // 1. pace 서버 계산, runningDate는 startedAt 기준 설정
    // 2. 기록 저장
    // 3. User.totalDistance 갱신
    // 4. 경험치 추가 → 레벨/티어 재계산
    //
    // TODO: 러닝 기록 입력 방식 미확정 (기획 논의 필요)
    //   방식 1) 앱 내 "시작/종료" 버튼으로 실시간 트래킹
    //     - startedAt/endedAt은 앱이 자동 설정
    //     - 서버에 러닝 세션 관리가 필요할 수 있음 (시작 API → 종료 API)
    //     - GPS 경로 데이터 전송 고려
    //   방식 2) 외부 앱 연동 or 수동 입력 후 일괄 저장
    //     - 현재 POST API 구조 그대로 사용 가능
    //     - 데이터 검증 강화 필요 (비정상 값 필터링)
    //   → 현재 API는 어떤 방식이든 호환되는 구조 (데이터를 받아서 저장)
    //
    // TODO: 경험치 획득 공식 미정 - 기획 확정 후 calculateExperience() 구현
    @Transactional
    fun createRunningRecord(userId: Long, request: RunningRecordCreateRequest): RunningRecordResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { BusinessException(ErrorCode.USER_NOT_FOUND) }

        // pace 계산: 총 시간(초) / 총 거리(km) → 초/km
        val pace = (request.duration / (request.distance / 1000.0)).toInt()

        val record = runningRecordRepository.save(
            RunningRecord(
                userId = userId,
                runningDate = request.startedAt.toLocalDate(),
                distance = request.distance,
                duration = request.duration,
                pace = pace,
                calories = request.calories,
                startedAt = request.startedAt,
                endedAt = request.endedAt,
                memo = request.memo
            )
        )

        // User.totalDistance 동기화
        user.totalDistance += request.distance

        // TODO: 경험치 획득 공식 미정 (거리/속도/시간 → XP 변환)
        //       기획 확정 후 아래 주석 해제 및 calculateExperience() 구현
        // val earnedXp = calculateExperience(request.distance, request.duration, pace)
        // user.experience += earnedXp
        // val (newLevel, newTier) = levelService.calculateLevelAndTier(user.experience)
        // user.level = newLevel
        // user.tier = newTier

        // 미션 달성 체크
        missionChecker.checkOnRunningRecordCreated(userId, record)

        return RunningRecordResponse.from(record)
    }

    // 주간 요약: DB 레벨 집계 쿼리로 합산 (별도 summary 테이블 없음)
    // - 주간 기준: 월요일 ~ 일요일 (ISO 8601)
    // - 글로벌 대응 시 getWeekStartDate() 로직만 변경하면 됨
    @Transactional(readOnly = true)
    fun getWeeklySummary(userId: Long, date: LocalDate): WeeklySummaryResponse {
        val startDate = getWeekStartDate(date)
        val endDate = startDate.plusDays(6)

        val totalDistance = runningRecordRepository.sumDistanceByUserIdAndDateRange(userId, startDate, endDate)
        val totalDuration = runningRecordRepository.sumDurationByUserIdAndDateRange(userId, startDate, endDate)
        val runDays = runningRecordRepository.countRunningDaysByUserIdAndDateRange(userId, startDate, endDate)
        val records = runningRecordRepository.findByUserIdAndDateRange(userId, startDate, endDate)

        val averagePace = if (totalDistance > 0) {
            (totalDuration / (totalDistance / 1000.0)).toInt()
        } else 0

        return WeeklySummaryResponse(
            startDate = startDate,
            endDate = endDate,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averagePace = averagePace,
            runCount = records.size,
            runDays = runDays
        )
    }

    // 주간 시작일 계산 (월요일 기준)
    // 글로벌 대응 시 이 메서드만 locale 기반으로 변경
    private fun getWeekStartDate(date: LocalDate): LocalDate {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    // 일일 기록: 해당 날짜의 개별 러닝 기록을 조회 후 서비스에서 합산
    // - 별도 summary 테이블 없이 running_records 실시간 집계
    // - 하루 러닝 횟수가 현실적으로 1~3회이므로 성능 이슈 없음
    @Transactional(readOnly = true)
    fun getDailyRecord(userId: Long, date: LocalDate): DailyRecordResponse {
        val records = runningRecordRepository.findByUserIdAndRunningDate(userId, date)

        val totalDistance = records.sumOf { it.distance }
        val totalDuration = records.sumOf { it.duration }
        // 평균 페이스: 총 시간(초) / 총 거리(km) → 초/km
        val averagePace = if (totalDistance > 0) {
            (totalDuration / (totalDistance / 1000.0)).toInt()
        } else 0

        return DailyRecordResponse(
            date = date,
            totalDistance = totalDistance,
            totalDuration = totalDuration,
            averagePace = averagePace,
            runCount = records.size,
            records = records.map { RunningRecordResponse.from(it) }
        )
    }

    @Transactional(readOnly = true)
    fun getRunningRecords(userId: Long, cursor: Long?, size: Int): RunningRecordListResponse {
        val pageable = PageRequest.of(0, size + 1)
        val records = runningRecordRepository.findByUserIdWithCursor(userId, cursor, pageable)

        val hasNext = records.size > size
        val content = if (hasNext) records.dropLast(1) else records

        return RunningRecordListResponse(
            records = content.map { RunningRecordResponse.from(it) },
            nextCursor = if (hasNext) content.last().id else null,
            hasNext = hasNext
        )
    }
}
