# 코딩 컨벤션

## 1. 코딩 스타일

- [Kotlin 공식 코딩 컨벤션](https://kotlinlang.org/docs/coding-conventions.html) 준수
- 들여쓰기: 4 spaces
- 최대 줄 길이: 120자

## 2. 네이밍 규칙

| 대상 | 규칙 | 예시 |
|------|------|------|
| 패키지 | 소문자, 단수형 | `user`, `auth`, `running` |
| 클래스 | PascalCase | `UserService`, `RunningRecord` |
| 함수/변수 | camelCase | `findByEmail`, `userName` |
| 상수 | SCREAMING_SNAKE_CASE | `MAX_RETRY_COUNT` |
| DTO | 접미사 `Request`, `Response` | `LoginRequest`, `UserResponse` |
| Entity | 도메인명 그대로 | `User`, `RunningRecord` |
| Repository | 접미사 `Repository` | `UserRepository` |
| Service | 접미사 `Service` | `UserService` |
| Controller | 접미사 `Controller` | `UserController` |

## 3. 패키지 구조

```
domain/{도메인}/
├── controller/   # API 엔드포인트
├── dto/          # Request/Response DTO
├── entity/       # Entity + Value Object
├── repository/   # 데이터 접근
└── service/      # Application Service (유스케이스)
```

### 역할 정의

| 계층 | 역할 | 주의사항 |
|------|------|----------|
| Controller | HTTP 요청/응답 처리 | 비즈니스 로직 금지 |
| Service | 유스케이스 조합, 트랜잭션 관리 | 도메인 로직은 Entity에 위임 |
| Entity | 비즈니스 규칙, 도메인 로직 | Anemic 모델 지양 |
| Repository | 데이터 저장/조회 | 쿼리 로직만 |
| DTO | 계층 간 데이터 전달 | 로직 없이 데이터만 |

## 4. 도메인 모델링

### Entity
- 고유 식별자(ID)를 가짐
- 상태 변경 가능 (mutable)
- 비즈니스 로직 포함

```kotlin
@Entity
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    var nickname: String,
    var email: String
) : BaseEntity() {

    fun updateNickname(newNickname: String) {
        require(newNickname.length in 2..20) { "닉네임은 2~20자여야 합니다" }
        this.nickname = newNickname
    }
}
```

### Value Object
- 식별자 없음, 값으로 동등성 판단
- 불변 (immutable)
- `data class` 사용

```kotlin
data class Pace(
    val minutes: Int,
    val seconds: Int
) {
    init {
        require(minutes >= 0) { "minutes must be non-negative" }
        require(seconds in 0..59) { "seconds must be 0-59" }
    }

    fun toSecondsPerKm(): Int = minutes * 60 + seconds
}
```

## 5. API 설계

### RESTful 원칙
| 메서드 | 용도 | 예시 |
|--------|------|------|
| GET | 조회 | `GET /api/users/{id}` |
| POST | 생성 | `POST /api/users` |
| PUT | 전체 수정 | `PUT /api/users/{id}` |
| PATCH | 부분 수정 | `PATCH /api/users/{id}` |
| DELETE | 삭제 | `DELETE /api/users/{id}` |

### 응답 포맷
모든 API는 `ApiResponse`로 감싸서 반환:

```kotlin
// 성공
ApiResponse.success(data)

// 실패
ApiResponse.error(errorCode, message)
```

## 6. Git 컨벤션

### 브랜치
```
feature/BE-XXX-간단한-설명
bugfix/BE-XXX-간단한-설명
hotfix/BE-XXX-간단한-설명
```

### 커밋 메시지 (Conventional Commits)
```
<type>(<scope>): <subject>

[optional body]
```

| Type | 용도 |
|------|------|
| feat | 새로운 기능 |
| fix | 버그 수정 |
| docs | 문서 변경 |
| style | 포맷팅, 세미콜론 등 |
| refactor | 리팩토링 |
| test | 테스트 추가/수정 |
| chore | 빌드, 설정 변경 |

예시:
```
feat(user): 회원가입 API 구현
fix(auth): JWT 토큰 만료 검증 오류 수정
docs(readme): 실행 방법 추가
```

## 7. 테스트

### 테스트 파일 위치
```
src/test/kotlin/com/runnershi/domain/user/
├── UserServiceTest.kt
├── UserRepositoryTest.kt
└── UserControllerTest.kt
```

### 테스트 네이밍
```kotlin
@Test
fun `유효한 이메일로 회원가입하면 성공한다`() { }

@Test
fun `중복된 이메일로 회원가입하면 실패한다`() { }
```
