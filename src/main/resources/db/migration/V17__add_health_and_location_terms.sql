-- Figma 약관 화면 순서에 맞춰 건강 데이터, 위치 정보 약관을 추가한다.
INSERT INTO terms (code, title, content_type, content_url, required, version, display_order) VALUES
('HEALTH_DATA', '건강 데이터 연동 동의', 'NONE', NULL, TRUE, 1, 3),
('LOCATION', '위치 정보 이용약관 동의', 'NONE', NULL, FALSE, 1, 4);

UPDATE terms SET display_order = 5 WHERE code = 'AGE_VERIFICATION';
UPDATE terms SET display_order = 6 WHERE code = 'MARKETING';
UPDATE terms SET display_order = 7 WHERE code = 'SNS';
