ALTER TABLE running_records
    ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

UPDATE level_config
SET required_experience = CASE
    WHEN level = 1 THEN 0
    WHEN level BETWEEN 2 AND 20 THEN (level - 1) * 100
    WHEN level BETWEEN 21 AND 40 THEN 1900 + (level - 20) * 130
    WHEN level BETWEEN 41 AND 70 THEN 4500 + (level - 40) * 170
    WHEN level BETWEEN 71 AND 100 THEN 9600 + (level - 70) * 220
    ELSE required_experience
END;
