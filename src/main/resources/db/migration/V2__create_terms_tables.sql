-- 약관 테이블 생성
CREATE TABLE terms (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    content_type VARCHAR(20) NOT NULL DEFAULT 'NONE',
    content_url VARCHAR(500),
    required BOOLEAN NOT NULL,
    version INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 약관 동의 테이블 생성
CREATE TABLE terms_agreements (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    terms_id BIGINT NOT NULL,
    agreed_version INT NOT NULL,
    agreed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_terms_agreements_terms FOREIGN KEY (terms_id) REFERENCES terms(id),
    CONSTRAINT uk_terms_agreements_user_terms UNIQUE (user_id, terms_id)
);

CREATE INDEX idx_terms_agreements_user_id ON terms_agreements (user_id);

-- 기본 약관 데이터 삽입
INSERT INTO terms (code, title, content_type, content_url, required, version, display_order) VALUES
('TERMS_OF_SERVICE', '서비스 이용약관 동의', 'NONE', NULL, TRUE, 1, 1),
('PRIVACY_POLICY', '개인정보 수집 및 이용 동의', 'NONE', NULL, TRUE, 1, 2),
('MARKETING', '마케팅 정보 수신 동의', 'NONE', NULL, FALSE, 1, 3),
('SNS', 'SNS 수신 동의', 'NONE', NULL, FALSE, 1, 4),
('AGE_VERIFICATION', '만 14세 이상입니다', 'NONE', NULL, TRUE, 1, 5);

-- User 테이블에서 약관 관련 컬럼 제거
ALTER TABLE users DROP COLUMN IF EXISTS terms_agreed_at;
ALTER TABLE users DROP COLUMN IF EXISTS privacy_agreed_at;
ALTER TABLE users DROP COLUMN IF EXISTS marketing_agreed_at;
