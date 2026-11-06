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

## 개발(dev) 서버

- 주소: `https://runnershi-dev-api.oguriworks.com` (상용과 같은 VM, 포트 8081, systemd `runnershi-dev`)
- DB: 상용과 같은 Supabase, 스키마만 `dev`로 분리. 상용 데이터는 건드리지 않는다.
- 프로필 `dev`에서만 `db/dev-seed/R__dev_seed.sql`(회원 20명, 60일 러닝 기록, 랭킹, 미션, 공지, 알림)이 들어간다. 상용에는 없다.
- 심사용 로그인(`REVIEW_LOGIN_CODE`)은 dev에서도 동작한다.
- 새 계정으로 로그인했는데 러닝 기록이 없으면 `DevDataSeeder`가 최근 두 달치 기록과 알림을 채운다.

### 테스트 로그인 (dev 전용, Maestro용)

```
POST /api/auth/dev
{"userKey": "e2e-1", "onboarded": true}
```

- 응답은 일반 로그인과 같다: `data.accessToken`, `data.refreshToken`, `data.isNewUser`.
- `userKey`가 같으면 같은 회원이다. `seed01`~`seed20`은 시드 회원(서울 강남구 8명 포함)이다.
- `onboarded=true`(기본): 약관 동의, 서울 강남구가 설정된 ACTIVE 회원. 기록이 없으면 샘플 기록을 채운다.
- `onboarded=false`: 가입 직후 상태(약관 동의 전, 지역 없음)로 온보딩 화면을 테스트한다.
- 상용에서는 이 경로가 404다.

### 약관 본문 고치는 법

- 원문은 `src/main/resources/terms/*.txt`이다.
- `python3 deploy/terms/build_terms.py <마이그레이션 번호> <version>`을 돌리면 Flyway SQL과 `deploy/oracle/pages/terms.html`, `privacy.html`이 다시 만들어진다.
- `GET /api/terms`의 각 항목에 `content`(plain text)와 `version`이 들어간다.

### 오류 응답

- 모든 오류는 `{"success":false,"data":null,"error":{"code":"...","message":"한국어 메시지"}}` 형태다.
- 400 요청 오류, 401 인증 필요, 403 권한 없음, 404 없는 주소·자원, 409 충돌, 415 지원하지 않는 형식, 500 서버 오류.
