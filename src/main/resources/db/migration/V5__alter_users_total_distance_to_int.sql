-- User.totalDistance 단위 변경: km(DOUBLE PRECISION) -> m(INT)
ALTER TABLE users ALTER COLUMN total_distance TYPE INT USING (total_distance * 1000)::INT;
ALTER TABLE users ALTER COLUMN total_distance SET DEFAULT 0;
