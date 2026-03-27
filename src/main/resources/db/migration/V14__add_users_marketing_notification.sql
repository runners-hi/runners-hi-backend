-- BE-033: 알림 설정 항목 추가
-- notificationEnabled: 푸시 알림 (기존)
-- marketingNotificationEnabled: 마케팅/이벤트 알림 (신규)
ALTER TABLE users
    ADD COLUMN marketing_notification_enabled BOOLEAN NOT NULL DEFAULT false;
