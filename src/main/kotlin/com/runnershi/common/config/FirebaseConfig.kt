package com.runnershi.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.io.File
import jakarta.annotation.PostConstruct

@Configuration
@Profile("!test")
class FirebaseConfig {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${firebase.credentials-path:}")
    private lateinit var credentialsPath: String

    // Firebase Admin SDK 초기화
    // 로컬: FIREBASE_CREDENTIALS_PATH에 서비스 계정 JSON 경로 지정
    // Cloud Run: Application Default Credentials 자동 사용
    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        try {
            val credentials = if (credentialsPath.isNotBlank() && File(credentialsPath).exists()) {
                File(credentialsPath).inputStream().use { GoogleCredentials.fromStream(it) }
            } else {
                GoogleCredentials.getApplicationDefault()
            }
            FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build())
            log.info("Firebase Admin SDK 초기화 완료")
        } catch (e: Exception) {
            // 자격 증명이 없어도 서버와 인앱 알림은 동작한다. 푸시만 보내지 않는다.
            log.warn("Firebase 자격 증명이 없어 푸시를 끕니다: {}", e.message)
        }
    }
}
