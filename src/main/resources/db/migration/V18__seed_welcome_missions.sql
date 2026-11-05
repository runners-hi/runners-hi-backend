-- 환영 미션: 앱의 미션 탭 첫 섹션. 가입 후 항상 활성이다.
WITH welcome AS (
    INSERT INTO mission_groups (name, description, type)
    VALUES ('Welcome Mission', '러너스하이에 오신 것을 환영합니다', 'WELCOME')
    RETURNING id
)
INSERT INTO missions (mission_group_id, name, description, image_url, condition_type)
SELECT welcome.id, seed.name, seed.description, seed.image_url, seed.condition_type
FROM welcome
CROSS JOIN (VALUES
    ('데뷔전', '첫 러닝 등록', 'https://runnershi-img.oguriworks.com/missions/mission_debut.png', 'FIRST_RUN'),
    ('집 앞 스타트', '지역 인증', 'https://runnershi-img.oguriworks.com/missions/mission_region.png', 'REGION_SET')
) AS seed(name, description, image_url, condition_type);
