-- BE-099: 인앱 알림 테이블
-- FCM Push는 별도 채널 (Firebase), 인앱 알림은 이 테이블로 관리
CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    title       VARCHAR(100) NOT NULL,
    body        VARCHAR(500) NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT false,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notifications_user_unread ON notifications (user_id, is_read, created_at DESC);
