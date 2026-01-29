-- 지역 테이블 생성
CREATE TABLE regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL
);

-- 특별시
INSERT INTO regions (name, type) VALUES ('서울특별시', 'SPECIAL_CITY');

-- 광역시
INSERT INTO regions (name, type) VALUES ('부산광역시', 'METROPOLITAN_CITY');
INSERT INTO regions (name, type) VALUES ('대구광역시', 'METROPOLITAN_CITY');
INSERT INTO regions (name, type) VALUES ('인천광역시', 'METROPOLITAN_CITY');
INSERT INTO regions (name, type) VALUES ('광주광역시', 'METROPOLITAN_CITY');
INSERT INTO regions (name, type) VALUES ('대전광역시', 'METROPOLITAN_CITY');
INSERT INTO regions (name, type) VALUES ('울산광역시', 'METROPOLITAN_CITY');

-- 특별자치시
INSERT INTO regions (name, type) VALUES ('세종특별자치시', 'SPECIAL_SELF_GOVERNING_CITY');

-- 특별자치도 (제주)
INSERT INTO regions (name, type) VALUES ('제주시', 'SPECIAL_SELF_GOVERNING_PROVINCE');
INSERT INTO regions (name, type) VALUES ('서귀포시', 'SPECIAL_SELF_GOVERNING_PROVINCE');

-- 경기도
INSERT INTO regions (name, type) VALUES ('고양시', 'CITY');
INSERT INTO regions (name, type) VALUES ('과천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('광명시', 'CITY');
INSERT INTO regions (name, type) VALUES ('광주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('구리시', 'CITY');
INSERT INTO regions (name, type) VALUES ('군포시', 'CITY');
INSERT INTO regions (name, type) VALUES ('김포시', 'CITY');
INSERT INTO regions (name, type) VALUES ('남양주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('동두천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('부천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('성남시', 'CITY');
INSERT INTO regions (name, type) VALUES ('수원시', 'CITY');
INSERT INTO regions (name, type) VALUES ('시흥시', 'CITY');
INSERT INTO regions (name, type) VALUES ('안산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('안성시', 'CITY');
INSERT INTO regions (name, type) VALUES ('안양시', 'CITY');
INSERT INTO regions (name, type) VALUES ('양주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('여주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('오산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('용인시', 'CITY');
INSERT INTO regions (name, type) VALUES ('의왕시', 'CITY');
INSERT INTO regions (name, type) VALUES ('의정부시', 'CITY');
INSERT INTO regions (name, type) VALUES ('이천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('파주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('평택시', 'CITY');
INSERT INTO regions (name, type) VALUES ('포천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('하남시', 'CITY');
INSERT INTO regions (name, type) VALUES ('화성시', 'CITY');

-- 강원도
INSERT INTO regions (name, type) VALUES ('강릉시', 'CITY');
INSERT INTO regions (name, type) VALUES ('동해시', 'CITY');
INSERT INTO regions (name, type) VALUES ('삼척시', 'CITY');
INSERT INTO regions (name, type) VALUES ('속초시', 'CITY');
INSERT INTO regions (name, type) VALUES ('원주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('춘천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('태백시', 'CITY');

-- 충청북도
INSERT INTO regions (name, type) VALUES ('제천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('청주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('충주시', 'CITY');

-- 충청남도
INSERT INTO regions (name, type) VALUES ('계룡시', 'CITY');
INSERT INTO regions (name, type) VALUES ('공주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('논산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('당진시', 'CITY');
INSERT INTO regions (name, type) VALUES ('보령시', 'CITY');
INSERT INTO regions (name, type) VALUES ('서산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('아산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('천안시', 'CITY');

-- 전라북도
INSERT INTO regions (name, type) VALUES ('군산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('김제시', 'CITY');
INSERT INTO regions (name, type) VALUES ('남원시', 'CITY');
INSERT INTO regions (name, type) VALUES ('익산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('전주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('정읍시', 'CITY');

-- 전라남도
INSERT INTO regions (name, type) VALUES ('광양시', 'CITY');
INSERT INTO regions (name, type) VALUES ('나주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('목포시', 'CITY');
INSERT INTO regions (name, type) VALUES ('순천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('여수시', 'CITY');

-- 경상북도
INSERT INTO regions (name, type) VALUES ('경산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('경주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('구미시', 'CITY');
INSERT INTO regions (name, type) VALUES ('김천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('문경시', 'CITY');
INSERT INTO regions (name, type) VALUES ('상주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('안동시', 'CITY');
INSERT INTO regions (name, type) VALUES ('영주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('영천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('포항시', 'CITY');

-- 경상남도
INSERT INTO regions (name, type) VALUES ('거제시', 'CITY');
INSERT INTO regions (name, type) VALUES ('김해시', 'CITY');
INSERT INTO regions (name, type) VALUES ('밀양시', 'CITY');
INSERT INTO regions (name, type) VALUES ('사천시', 'CITY');
INSERT INTO regions (name, type) VALUES ('양산시', 'CITY');
INSERT INTO regions (name, type) VALUES ('진주시', 'CITY');
INSERT INTO regions (name, type) VALUES ('창원시', 'CITY');
INSERT INTO regions (name, type) VALUES ('통영시', 'CITY');
