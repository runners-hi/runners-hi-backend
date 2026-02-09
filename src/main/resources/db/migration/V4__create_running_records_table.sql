-- 러닝 기록 테이블 생성
CREATE TABLE running_records (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    running_date DATE NOT NULL,

    distance INT NOT NULL,
    duration INT NOT NULL,
    pace INT NOT NULL,
    calories INT,

    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NOT NULL,

    memo VARCHAR(500),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_running_records_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_running_records_user_date ON running_records (user_id, running_date);
CREATE INDEX idx_running_records_user_created ON running_records (user_id, created_at);
