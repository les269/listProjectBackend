-- Run once for each existing dynamic database (SQLite or PostgreSQL).
-- New databases already include this column in their initialization scripts.
ALTER TABLE spider_config ADD COLUMN request_interval_ms BIGINT NOT NULL DEFAULT 0;
