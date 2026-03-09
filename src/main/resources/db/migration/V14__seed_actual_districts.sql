-- 실제 구/군 데이터가 있는 지역에만 non-default district를 추가한다.
-- default district는 기존 유저 백필 및 하위 지역 미선택 fallback 용도로 유지한다.

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('강남구'),
        ('강동구'),
        ('강북구'),
        ('강서구'),
        ('관악구'),
        ('광진구'),
        ('구로구'),
        ('금천구'),
        ('노원구'),
        ('도봉구'),
        ('동대문구'),
        ('동작구'),
        ('마포구'),
        ('서대문구'),
        ('서초구'),
        ('성동구'),
        ('성북구'),
        ('송파구'),
        ('양천구'),
        ('영등포구'),
        ('용산구'),
        ('은평구'),
        ('종로구'),
        ('중구'),
        ('중랑구')
) AS v(name) ON TRUE
WHERE r.name = '서울특별시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('강서구'),
        ('금정구'),
        ('기장군'),
        ('남구'),
        ('동구'),
        ('동래구'),
        ('부산진구'),
        ('북구'),
        ('사상구'),
        ('사하구'),
        ('서구'),
        ('수영구'),
        ('연제구'),
        ('영도구'),
        ('중구'),
        ('해운대구')
) AS v(name) ON TRUE
WHERE r.name = '부산광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('군위군'),
        ('남구'),
        ('달서구'),
        ('달성군'),
        ('동구'),
        ('북구'),
        ('서구'),
        ('수성구'),
        ('중구')
) AS v(name) ON TRUE
WHERE r.name = '대구광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('강화군'),
        ('계양구'),
        ('남동구'),
        ('동구'),
        ('미추홀구'),
        ('부평구'),
        ('서구'),
        ('연수구'),
        ('옹진군'),
        ('중구')
) AS v(name) ON TRUE
WHERE r.name = '인천광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('광산구'),
        ('남구'),
        ('동구'),
        ('북구'),
        ('서구')
) AS v(name) ON TRUE
WHERE r.name = '광주광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('대덕구'),
        ('동구'),
        ('서구'),
        ('유성구'),
        ('중구')
) AS v(name) ON TRUE
WHERE r.name = '대전광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('남구'),
        ('동구'),
        ('북구'),
        ('울주군'),
        ('중구')
) AS v(name) ON TRUE
WHERE r.name = '울산광역시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('덕양구'),
        ('일산동구'),
        ('일산서구')
) AS v(name) ON TRUE
WHERE r.name = '고양시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('분당구'),
        ('수정구'),
        ('중원구')
) AS v(name) ON TRUE
WHERE r.name = '성남시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('권선구'),
        ('영통구'),
        ('장안구'),
        ('팔달구')
) AS v(name) ON TRUE
WHERE r.name = '수원시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('단원구'),
        ('상록구')
) AS v(name) ON TRUE
WHERE r.name = '안산시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('동안구'),
        ('만안구')
) AS v(name) ON TRUE
WHERE r.name = '안양시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('기흥구'),
        ('수지구'),
        ('처인구')
) AS v(name) ON TRUE
WHERE r.name = '용인시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('상당구'),
        ('서원구'),
        ('청원구'),
        ('흥덕구')
) AS v(name) ON TRUE
WHERE r.name = '청주시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('동남구'),
        ('서북구')
) AS v(name) ON TRUE
WHERE r.name = '천안시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('덕진구'),
        ('완산구')
) AS v(name) ON TRUE
WHERE r.name = '전주시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('남구'),
        ('북구')
) AS v(name) ON TRUE
WHERE r.name = '포항시';

INSERT INTO districts (region_id, name, is_default)
SELECT r.id, v.name, FALSE
FROM regions r
JOIN (
    VALUES
        ('마산합포구'),
        ('마산회원구'),
        ('성산구'),
        ('의창구'),
        ('진해구')
) AS v(name) ON TRUE
WHERE r.name = '창원시';
