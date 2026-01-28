-- User 테이블 생성
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    -- 소셜 로그인
    provider VARCHAR(20) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),

    -- 프로필
    nickname VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(500),

    -- 지역
    region_id BIGINT,

    -- 등급/레벨
    tier VARCHAR(20) NOT NULL DEFAULT 'BRONZE',
    level INT NOT NULL DEFAULT 1,
    experience INT NOT NULL DEFAULT 0,
    total_distance DOUBLE PRECISION NOT NULL DEFAULT 0.0,

    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    -- 약관
    terms_agreed_at TIMESTAMP,
    privacy_agreed_at TIMESTAMP,
    marketing_agreed_at TIMESTAMP,

    -- 알림
    notification_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    fcm_token VARCHAR(500),

    -- 토큰
    refresh_token VARCHAR(500),
    refresh_token_expires_at TIMESTAMP,

    -- 시간
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,

    -- 제약조건
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id),
    CONSTRAINT uk_users_nickname UNIQUE (nickname)
);

-- 인덱스
CREATE INDEX idx_users_region_distance ON users (region_id, total_distance);
