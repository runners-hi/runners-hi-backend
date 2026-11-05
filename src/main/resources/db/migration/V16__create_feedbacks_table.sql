-- 마이페이지 '개발자에게'로 받은 의견
CREATE TABLE feedbacks (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT        NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    created_at  TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_feedbacks_created_at ON feedbacks (created_at DESC);
CREATE INDEX idx_feedbacks_user_id ON feedbacks (user_id);
