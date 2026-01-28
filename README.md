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
# 컨테이너 시작
docker compose up -d

# 상태 확인 (healthy 상태인지 확인)
docker compose ps

# 로그 확인
docker compose logs -f postgres

# 컨테이너 종료
docker compose down

# 컨테이너 + 데이터 삭제 (초기화)
docker compose down -v
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

| 파일 | 용도 | Swagger | 로깅 |
|------|------|---------|------|
| `application.yaml` | 공통 설정 | - | - |
| `application-local.yaml` | 로컬 개발 | O | DEBUG |
| `application-dev.yaml` | 개발 서버 | O | INFO |
| `application-prod.yaml` | 운영 서버 | X | WARN |
| `application-test.yaml` | 테스트 (H2) | - | DEBUG |

## 환경 변수

| 변수 | 필수 환경 | 설명 |
|------|-----------|------|
| `DB_URL` | dev, prod | JDBC 연결 URL |
| `DB_USERNAME` | dev, prod | DB 사용자 |
| `DB_PASSWORD` | dev, prod | DB 비밀번호 |
| `JWT_SECRET` | prod | JWT 서명 키 (256bit 이상) |

> local 환경은 기본값이 설정되어 있어 환경 변수 없이 실행 가능

### 환경 변수 설정 방법

```bash
# .env.example을 복사하여 .env.local 생성
cp .env.example .env.local

# .env.local 수정 후 사용
source .env.local && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

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
