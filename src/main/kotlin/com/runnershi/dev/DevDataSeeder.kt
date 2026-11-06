package com.runnershi.dev

import com.runnershi.domain.auth.service.LoginHook
import com.runnershi.domain.notification.entity.Notification
import com.runnershi.domain.notification.entity.NotificationType
import com.runnershi.domain.notification.repository.NotificationRepository
import com.runnershi.domain.running.dto.RunningRecordCreateRequest
import com.runnershi.domain.running.entity.RunningRecordSource
import com.runnershi.domain.running.repository.RunningRecordRepository
import com.runnershi.domain.running.service.RunningRecordService
import com.runnershi.domain.user.entity.User
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 개발(dev) 서버 전용. 로그인한 회원에게 기록이 하나도 없으면 최근 두 달치 샘플 러닝 기록과 알림을 넣어,
 * 새 계정으로 로그인해도 모든 화면에 내용이 보이게 한다. 상용(prod)에는 만들어지지 않는다.
 */
@Component
@Profile("dev")
class DevDataSeeder(
    private val runningRecordRepository: RunningRecordRepository,
    private val runningRecordService: RunningRecordService,
    private val notificationRepository: NotificationRepository
) : LoginHook {

    override fun onLogin(user: User) {
        seedIfEmpty(user)
    }

    /** 기록이 이미 있으면 아무것도 하지 않는다. 넣었으면 true. */
    fun seedIfEmpty(user: User): Boolean {
        if (runningRecordRepository.existsByUserId(user.id)) return false

        val now = LocalDateTime.now()
        // 오래된 것부터 저장해야 누적 거리·경험치·미션이 시간순으로 쌓인다
        DAYS_AGO.forEachIndexed { i, daysAgo ->
            val distance = DISTANCES[i % DISTANCES.size]
            val pace = PACES[i % PACES.size]
            val duration = (distance / 1000.0 * pace).toInt()
            val startedAt = now.minusDays(daysAgo.toLong()).withHour(6 + i % 3).withMinute(10 + i * 7 % 45).withSecond(0).withNano(0)
            runningRecordService.createRunningRecord(
                user.id,
                RunningRecordCreateRequest(
                    distance = distance,
                    duration = duration,
                    calories = (distance / 1000.0 * 62).toInt(),
                    startedAt = startedAt,
                    endedAt = startedAt.plusSeconds(duration.toLong()),
                    source = SOURCES[i % SOURCES.size],
                    memo = if (i % 5 == 0) "개발 서버 샘플 기록" else null
                )
            )
        }

        notificationRepository.save(
            Notification(user.id, NotificationType.SYSTEM, "러너스하이에 오신 걸 환영해요", "개발 서버의 샘플 데이터가 준비됐어요.", true, now.minusDays(40))
        )
        notificationRepository.save(
            Notification(user.id, NotificationType.LEVEL_UP, "레벨 업!", "새 레벨이 되었어요", false, now.minusDays(1))
        )
        return true
    }

    private companion object {
        // 최근 두 달 안, 오래된 날부터
        val DAYS_AGO = listOf(57, 54, 51, 48, 45, 41, 38, 35, 31, 28, 25, 21, 18, 14, 11, 8, 5, 3, 1)
        val DISTANCES = listOf(5200, 7800, 3400, 10100, 6400, 8900, 4600, 12300)
        val PACES = listOf(335, 318, 352, 301, 327, 343)
        val SOURCES = listOf(RunningRecordSource.HEALTH_CONNECT, RunningRecordSource.HEALTH_KIT, RunningRecordSource.MANUAL)
    }
}
