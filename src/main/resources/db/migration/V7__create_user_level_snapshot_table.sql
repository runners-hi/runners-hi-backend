-- 유저 레벨/티어 스냅샷 테이블
-- 연도별/월별 레벨, 티어, 경험치, 총 거리를 기록
-- 연초 초기화 시 직전년도 최종 티어 확인 및 통계 데이터 활용
CREATE TABLE user_level_snapshot (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    year INT NOT NULL,
    month INT NOT NULL,
    level INT NOT NULL,
    tier VARCHAR(20) NOT NULL,
    experience INT NOT NULL,
    total_distance INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_level_snapshot_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_user_level_snapshot UNIQUE (user_id, year, month)
);

CREATE INDEX idx_user_level_snapshot_user_year ON user_level_snapshot (user_id, year);
