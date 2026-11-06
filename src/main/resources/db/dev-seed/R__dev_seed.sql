-- 개발 서버(dev 프로필)에서만 읽는 샘플 데이터. 상용(prod)은 db/migration 만 읽는다.
-- 반복 마이그레이션이라 파일 내용이 바뀔 때마다 다시 실행되고, 시드 행만 지웠다가 다시 넣는다.
-- 시드 회원의 로그인 식별자는 'dev:seed01' ~ 'dev:seed20' 이고, POST /api/auth/dev 로 같은 키(seed01)로 로그인한다.

-- 1. 이전 시드 지우기
DELETE FROM notifications WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM user_missions WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM user_missions WHERE mission_id IN (
    SELECT m.id FROM missions m JOIN mission_groups g ON g.id = m.mission_group_id
    WHERE g.name IN ('11월 러닝 챌린지', '겨울 러닝 페스티벌'));
DELETE FROM missions WHERE mission_group_id IN (SELECT id FROM mission_groups WHERE name IN ('11월 러닝 챌린지', '겨울 러닝 페스티벌'));
DELETE FROM mission_groups WHERE name IN ('11월 러닝 챌린지', '겨울 러닝 페스티벌');
DELETE FROM running_records WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM user_level_snapshot WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM terms_agreements WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM feedbacks WHERE user_id IN (SELECT id FROM users WHERE provider_id LIKE 'dev:seed%');
DELETE FROM users WHERE provider_id LIKE 'dev:seed%';
DELETE FROM notices WHERE title IN (
    '러너스하이 정식 오픈 안내', '11월 러닝 챌린지가 시작됐어요', '지역 랭킹 집계 기준 안내',
    '건강 데이터 연동 방법 안내', '서버 점검 안내', '이용약관·개인정보 처리방침 개정 안내');

-- 2. 회원 20명 (여러 지역)
INSERT INTO users (provider, provider_id, email, nickname, region_id, district_id, status, notification_enabled, last_login_at)
SELECT 'KAKAO', 'dev:seed' || lpad(v.n::text, 2, '0'), NULL, v.nickname, r.id,
       COALESCE((SELECT d.id FROM districts d WHERE d.region_id = r.id AND d.name = v.district),
                (SELECT d.id FROM districts d WHERE d.region_id = r.id AND d.is_default)),
       'ACTIVE', TRUE, now() - (v.n || ' hours')::interval
FROM (VALUES
    (1,  '새벽바람러너',   '서울특별시',   '강남구'),
    (2,  '한강치타',       '서울특별시',   '강남구'),
    (3,  '민지의아침런',   '서울특별시',   '강남구'),
    (4,  '퇴근후조깅',     '서울특별시',   '강남구'),
    (5,  '러닝초보탈출',   '서울특별시',   '강남구'),
    (6,  '강남페이서',     '서울특별시',   '강남구'),
    (7,  '주말마라토너',   '서울특별시',   '강남구'),
    (8,  '달리는서연',     '서울특별시',   '강남구'),
    (9,  '송파번개',       '서울특별시',   '송파구'),
    (10, '잠실러너',       '서울특별시',   '송파구'),
    (11, '마포바람',       '서울특별시',   '마포구'),
    (12, '해운대파도',     '부산광역시',   '해운대구'),
    (13, '광안리선셋런',   '부산광역시',   '수영구'),
    (14, '수성못러너',     '대구광역시',   '수성구'),
    (15, '송도달리기',     '인천광역시',   '연수구'),
    (16, '판교러너',       '성남시',       '분당구'),
    (17, '수원화성런',     '수원시',       '영통구'),
    (18, '제주오름러너',   '제주시',       NULL),
    (19, '대전꿈돌이런',   '대전광역시',   '유성구'),
    (20, '무등산러너',     '광주광역시',   '북구')
) AS v(n, nickname, region, district)
JOIN regions r ON r.name = v.region;

-- 3. 최근 60일 러닝 기록 (회원마다 빈도·거리·페이스가 다르다)
INSERT INTO running_records (user_id, running_date, distance, duration, pace, calories, started_at, ended_at, source, memo, created_at, updated_at)
SELECT x.user_id, x.running_date, x.distance,
       round(x.distance / 1000.0 * x.pace)::int,
       x.pace,
       round(x.distance / 1000.0 * 62)::int,
       x.started_at,
       x.started_at + (round(x.distance / 1000.0 * x.pace)::int || ' seconds')::interval,
       x.source,
       CASE WHEN x.d % 9 = 0 THEN '컨디션 좋은 날' WHEN x.d % 11 = 0 THEN '비 온 뒤라 천천히' ELSE NULL END,
       x.started_at + (round(x.distance / 1000.0 * x.pace)::int || ' seconds')::interval,
       x.started_at + (round(x.distance / 1000.0 * x.pace)::int || ' seconds')::interval
FROM (
    SELECT u.id AS user_id, g.d,
           (current_date - g.d) AS running_date,
           (round((p.base_m * (70 + (p.n * 13 + g.d * 29) % 61) / 100.0) / 10.0) * 10)::int AS distance,
           (p.pace + ((p.n * 7 + g.d * 11) % 41) - 20) AS pace,
           (current_date - g.d)::timestamp + ((6 + (p.n + g.d) % 3) || ' hours')::interval + (((p.n * 5 + g.d * 7) % 50) || ' minutes')::interval AS started_at,
           (ARRAY['HEALTH_CONNECT', 'HEALTH_KIT', 'MANUAL'])[1 + (p.n + g.d) % 3] AS source
    FROM (VALUES
        (1, 85, 8000, 300), (2, 75, 10000, 285), (3, 60, 5000, 360), (4, 50, 6000, 345),
        (5, 8, 3000, 420), (6, 90, 12000, 270), (7, 40, 15000, 330), (8, 55, 7000, 350),
        (9, 70, 9000, 300), (10, 45, 6000, 340), (11, 65, 7500, 315), (12, 80, 9000, 310),
        (13, 35, 5000, 370), (14, 50, 8000, 325), (15, 60, 7000, 335), (16, 70, 10000, 305),
        (17, 40, 6500, 355), (18, 12, 5000, 380), (19, 55, 9000, 320), (20, 45, 7000, 345)
    ) AS p(n, pct, base_m, pace)
    JOIN users u ON u.provider_id = 'dev:seed' || lpad(p.n::text, 2, '0')
    CROSS JOIN generate_series(0, 59) AS g(d)
    WHERE (p.n * 37 + g.d * 53) % 100 < p.pct
      AND ((current_date - g.d)::timestamp + ((6 + (p.n + g.d) % 3) || ' hours')::interval) < now()
) x;

-- 4. 누적 거리, 경험치, 레벨, 티어 (기록 합계에서 계산)
UPDATE users u SET
    total_distance = s.total_m,
    experience = s.exp,
    level = COALESCE((SELECT max(c.level) FROM level_config c WHERE c.required_experience <= s.exp), 1),
    tier = CASE
        WHEN COALESCE((SELECT max(c.level) FROM level_config c WHERE c.required_experience <= s.exp), 1) <= 5 THEN 'BRONZE'
        WHEN COALESCE((SELECT max(c.level) FROM level_config c WHERE c.required_experience <= s.exp), 1) <= 20 THEN 'SILVER'
        WHEN COALESCE((SELECT max(c.level) FROM level_config c WHERE c.required_experience <= s.exp), 1) <= 40 THEN 'GOLD'
        WHEN COALESCE((SELECT max(c.level) FROM level_config c WHERE c.required_experience <= s.exp), 1) <= 70 THEN 'PLATINUM'
        ELSE 'DIAMOND' END
FROM (
    SELECT r.user_id, sum(r.distance)::int AS total_m, (sum(r.distance) * 0.02)::int AS exp
    FROM running_records r JOIN users x ON x.id = r.user_id
    WHERE x.provider_id LIKE 'dev:seed%' GROUP BY r.user_id
) s
WHERE u.id = s.user_id;

-- 5. 지난달 레벨 스냅샷
INSERT INTO user_level_snapshot (user_id, year, month, level, tier, experience, total_distance)
SELECT id, extract(year FROM now() - interval '1 month')::int, extract(month FROM now() - interval '1 month')::int,
       GREATEST(1, level - 2), tier, (experience * 0.8)::int, (total_distance * 0.8)::int
FROM users WHERE provider_id LIKE 'dev:seed%';

-- 6. 약관 동의
INSERT INTO terms_agreements (user_id, terms_id, agreed_version, agreed_at)
SELECT u.id, t.id, t.version, now() - interval '50 days'
FROM users u CROSS JOIN terms t WHERE u.provider_id LIKE 'dev:seed%';

-- 7. 미션 그룹과 미션
WITH g AS (
    INSERT INTO mission_groups (name, description, type, start_date, end_date, show_on_home)
    VALUES ('11월 러닝 챌린지', '11월 한 달 동안 달리고 보상을 모아 보세요', 'EVENT', date '2026-11-01', date '2027-12-31', TRUE)
    RETURNING id
)
INSERT INTO missions (mission_group_id, name, description, condition_type, condition_value)
SELECT g.id, m.name, m.description, m.condition_type, m.condition_value FROM g CROSS JOIN (VALUES
    ('누적 50km', '누적 거리 50km 달리기', 'CUMULATIVE_DISTANCE', 50000),
    ('누적 100km', '누적 거리 100km 달리기', 'CUMULATIVE_DISTANCE', 100000),
    ('10K 완주', '한 번에 10km 달리기', 'SINGLE_DISTANCE', 10000),
    ('5분대 페이스', 'km당 5분 30초 이하로 달리기', 'PACE', 330)
) AS m(name, description, condition_type, condition_value);

WITH g AS (
    INSERT INTO mission_groups (name, description, type, start_date, end_date, show_on_home)
    VALUES ('겨울 러닝 페스티벌', '추운 날씨에도 멈추지 않는 러너를 위한 이벤트', 'EVENT', date '2026-12-01', date '2027-02-28', FALSE)
    RETURNING id
)
INSERT INTO missions (mission_group_id, name, description, condition_type, condition_value, condition_start_date, condition_end_date)
SELECT g.id, m.name, m.description, m.condition_type, NULL, m.s, m.e FROM g CROSS JOIN (VALUES
    ('첫눈 러닝', '12월에 한 번 달리기', 'RUN_IN_PERIOD', date '2026-12-01', date '2026-12-31'),
    ('새해 첫 러닝', '1월 1일에 달리기', 'RUN_ON_DATE', date '2027-01-01', date '2027-01-01')
) AS m(name, description, condition_type, s, e);

-- 8. 회원별 미션 진행 (환영 미션 + 챌린지)
INSERT INTO user_missions (user_id, mission_id, status, current_value, achieved_at)
SELECT q.user_id, q.mission_id, q.status, q.current_value,
       CASE WHEN q.status = 'ACHIEVED' THEN now() - interval '3 days' END
FROM (
    SELECT u.id AS user_id, m.id AS mission_id,
           CASE m.condition_type
               WHEN 'FIRST_RUN' THEN 'ACHIEVED'
               WHEN 'REGION_SET' THEN 'ACHIEVED'
               WHEN 'CUMULATIVE_DISTANCE' THEN CASE WHEN u.total_distance >= m.condition_value THEN 'ACHIEVED' WHEN u.total_distance > 0 THEN 'IN_PROGRESS' END
               WHEN 'SINGLE_DISTANCE' THEN CASE WHEN COALESCE(s.max_d, 0) >= m.condition_value THEN 'ACHIEVED' END
               WHEN 'PACE' THEN CASE WHEN COALESCE(s.min_p, 99999) <= m.condition_value THEN 'ACHIEVED' END
           END AS status,
           CASE m.condition_type
               WHEN 'CUMULATIVE_DISTANCE' THEN LEAST(u.total_distance, m.condition_value)
               ELSE COALESCE(m.condition_value, 0)
           END AS current_value
    FROM users u
    JOIN missions m ON m.condition_type IN ('FIRST_RUN', 'REGION_SET', 'CUMULATIVE_DISTANCE', 'SINGLE_DISTANCE', 'PACE')
    LEFT JOIN (SELECT user_id, max(distance) AS max_d, min(pace) AS min_p FROM running_records GROUP BY user_id) s ON s.user_id = u.id
    WHERE u.provider_id LIKE 'dev:seed%'
) q
WHERE q.status IS NOT NULL;

-- 9. 공지사항
INSERT INTO notices (title, content, is_active, created_at, updated_at) VALUES
('러너스하이 정식 오픈 안내', E'러너스하이가 문을 열었어요.\n건강 앱의 러닝 기록을 가져와 레벨과 지역 랭킹을 확인해 보세요.', TRUE, now() - interval '30 days', now() - interval '30 days'),
('11월 러닝 챌린지가 시작됐어요', E'11월 한 달 동안 누적 거리와 10K 완주 미션에 도전해 보세요.\n달성하면 알림으로 알려 드려요.', TRUE, now() - interval '6 days', now() - interval '6 days'),
('지역 랭킹 집계 기준 안내', E'랭킹은 같은 구·군에서 누적 러닝 거리가 많은 순서로 집계해요.\n거리가 같으면 먼저 가입한 회원이 앞서요.', TRUE, now() - interval '20 days', now() - interval '20 days'),
('건강 데이터 연동 방법 안내', E'안드로이드는 Health Connect, 아이폰은 건강 앱에서 러닝 기록을 가져와요.\n설정에서 권한을 허용해 주세요.', TRUE, now() - interval '25 days', now() - interval '25 days'),
('서버 점검 안내', E'더 안정적인 서비스를 위해 새벽 시간에 짧게 점검할 수 있어요.\n점검 중에는 잠시 접속이 어려울 수 있어요.', TRUE, now() - interval '2 days', now() - interval '2 days'),
('이용약관·개인정보 처리방침 개정 안내', E'이용약관과 개인정보 처리방침이 개정됐어요.\n랭킹 공개 범위, 탈퇴 시 삭제 항목, 접속 기록 보관 기간을 자세히 적었어요.', TRUE, now() - interval '1 days', now() - interval '1 days');

-- 10. 알림 (회원마다 시스템 알림 + 미션 달성 알림)
INSERT INTO notifications (user_id, type, title, body, is_read, created_at)
SELECT u.id, 'SYSTEM', '러너스하이에 오신 걸 환영해요', '오늘부터 달린 거리가 지역 랭킹에 반영돼요.', TRUE, now() - interval '40 days'
FROM users u WHERE u.provider_id LIKE 'dev:seed%';
INSERT INTO notifications (user_id, type, title, body, is_read, created_at)
SELECT u.id, 'MISSION_ACHIEVED', '미션 달성!', '"누적 50km" 미션을 달성했습니다', FALSE, now() - interval '3 days'
FROM users u WHERE u.provider_id LIKE 'dev:seed%' AND u.total_distance >= 50000;
INSERT INTO notifications (user_id, type, title, body, is_read, created_at)
SELECT u.id, 'LEVEL_UP', '레벨 업!', 'Lv.' || u.level || ' 이 되었어요', FALSE, now() - interval '1 days'
FROM users u WHERE u.provider_id LIKE 'dev:seed%' AND u.level > 5;

-- 11. 앱 버전 (없을 때만)
INSERT INTO app_versions (platform, min_version, latest_version) VALUES ('IOS', '1.0.0', '1.0.0'), ('ANDROID', '1.0.0', '1.0.0')
ON CONFLICT (platform) DO NOTHING;
