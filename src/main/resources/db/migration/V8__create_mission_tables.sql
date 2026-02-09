-- 미션 그룹 (이벤트 단위)
CREATE TABLE mission_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(20) NOT NULL,
    start_date DATE,
    end_date DATE,
    image_url VARCHAR(500),
    show_on_home BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 개별 미션
CREATE TABLE missions (
    id BIGSERIAL PRIMARY KEY,
    mission_group_id BIGINT NOT NULL REFERENCES mission_groups(id),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    image_url VARCHAR(500),
    condition_type VARCHAR(30) NOT NULL,
    condition_value INT,
    condition_start_date DATE,
    condition_end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_missions_group_id ON missions(mission_group_id);

-- 유저별 미션 달성 상태
CREATE TABLE user_missions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mission_id BIGINT NOT NULL REFERENCES missions(id),
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_ACHIEVED',
    current_value INT NOT NULL DEFAULT 0,
    achieved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, mission_id)
);

CREATE INDEX idx_user_missions_user_id ON user_missions(user_id);
