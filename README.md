# Runners-Hi Backend

러닝 기록 관리 서비스 백엔드 API

## 기술 스택

- Kotlin 2.2 + Spring Boot 4.0
- PostgreSQL 16 + Flyway
- Spring Security + JWT
- Swagger (springdoc-openapi)

## 실행 방법

### 1. PostgreSQL 시작
```bash
docker-compose up -d
```

### 2. 애플리케이션 실행
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

### 3. API 문서 확인
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## 프로젝트 구조

```
src/main/kotlin/com/runnershi/
├── RunnersHiBackendApplication.kt
├── common/              # 공통 모듈 (비즈니스 무관)
│   ├── config/          # 설정 (Security, JPA, Swagger)
│   ├── entity/          # BaseEntity (id, createdAt, updatedAt)
│   ├── exception/       # 에러 코드, 전역 예외 처리
│   ├── response/        # API 응답 포맷
│   └── security/        # JWT 인증
└── domain/              # 비즈니스 로직
    ├── user/            # 사용자
    ├── auth/            # 인증
    └── running/         # 러닝 기록
```

## 설정 파일

| 파일 | 용도 |
|------|------|
| `application.yaml` | 공통 설정 |
| `application-local.yaml` | 로컬 개발 환경 |
| `application-test.yaml` | 테스트 환경 (H2) |

## 환경 변수

| 변수 | 기본값 | 설명 |
|------|--------|------|
| `DB_USERNAME` | postgres | DB 사용자 |
| `DB_PASSWORD` | password | DB 비밀번호 |
| `JWT_SECRET` | (개발용 기본값) | JWT 서명 키 |

## 주요 명령어

```bash
# 빌드
./gradlew build

# 테스트
./gradlew test

# 실행 (local 프로필)
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

## DB 접속 정보 (로컬)

| 항목 | 값 |
|------|-----|
| Host | localhost:5432 |
| Database | runners_hi |
| Username | postgres |
| Password | password |
