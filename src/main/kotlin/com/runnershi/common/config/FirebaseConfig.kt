package com.runnershi.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.File
import jakarta.annotation.PostConstruct

@Configuration
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

        val options = if (credentialsPath.isNotBlank() && File(credentialsPath).exists()) {
            val credentials = GoogleCredentials.fromStream(File(credentialsPath).inputStream())
            FirebaseOptions.builder().setCredentials(credentials).build()
        } else {
            log.info("Firebase: Application Default Credentials 사용")
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .build()
        }

        FirebaseApp.initializeApp(options)
        log.info("Firebase Admin SDK 초기화 완료")
    }
}
