-- BE-094: RunningRecord 추가 필드
-- GPS 경로/날씨는 위치사업자 등록/외부 API 필요로 제외
-- 케이던스는 MVP 범위 외 제외
ALTER TABLE running_records
    ADD COLUMN heart_rate_avg  INT,
    ADD COLUMN heart_rate_max  INT,
    ADD COLUMN elevation_gain  INT,
    ADD COLUMN elevation_loss  INT,
    ADD COLUMN running_type    VARCHAR(20);
