CREATE TABLE districts (
    id BIGSERIAL PRIMARY KEY,
    region_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_districts_region FOREIGN KEY (region_id) REFERENCES regions (id),
    CONSTRAINT uk_districts_region_name UNIQUE (region_id, name)
);

CREATE INDEX idx_districts_region_name ON districts (region_id, name);

INSERT INTO districts (region_id, name, is_default)
SELECT id, name, TRUE
FROM regions;

ALTER TABLE users
    ADD COLUMN district_id BIGINT;

UPDATE users u
SET district_id = d.id
FROM districts d
WHERE u.region_id = d.region_id
  AND d.is_default = TRUE;

ALTER TABLE users
    ADD CONSTRAINT fk_users_district FOREIGN KEY (district_id) REFERENCES districts (id);

CREATE INDEX idx_users_district_distance ON users (district_id, total_distance);
