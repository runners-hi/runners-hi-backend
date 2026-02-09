CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(20) NOT NULL UNIQUE,
    min_version VARCHAR(20) NOT NULL,
    latest_version VARCHAR(20) NOT NULL,
    update_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 초기값 (출시 후 운영팀이 업데이트)
INSERT INTO app_versions (platform, min_version, latest_version) VALUES
('IOS', '1.0.0', '1.0.0'),
('ANDROID', '1.0.0', '1.0.0');
