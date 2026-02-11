CREATE TABLE IF NOT EXISTS database_config (
    config_id TEXT PRIMARY KEY,
    config_name TEXT NOT NULL UNIQUE,
    database_type TEXT NOT NULL,  -- 'SQLite', 'PostgreSQL', 'MySQL', 'MariaDB', 'Oracle', etc.
    jdbc_url TEXT NOT NULL,  -- 完整的 JDBC URL，例如: jdbc:sqlite:/path/to/db.sqlite 或 jdbc:postgresql://host:port/database
    driver_class_name TEXT NOT NULL,  -- Driver class name，例如: org.sqlite.JDBC, org.postgresql.Driver
    host TEXT,  -- 主機地址（SQLite 不需要）
    port INTEGER,  -- 埠號（SQLite 不需要）
    database_name TEXT,  -- 資料庫名稱
    username TEXT,  -- 使用者名稱（SQLite 不需要）
    password TEXT,  -- 密碼（SQLite 不需要，建議加密儲存）
    hibernate_dialect TEXT,  -- Hibernate Dialect，例如: org.hibernate.community.dialect.SQLiteDialect
    additional_properties TEXT,  -- 其他設定（JSON 格式），例如: {"ssl": true, "connectionTimeout": 30000}
    enabled INTEGER NOT NULL DEFAULT 1,  -- 是否啟用 (1=啟用, 0=停用)
    description TEXT,  -- 描述說明
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 建立索引以加快查詢
CREATE INDEX IF NOT EXISTS idx_database_config_name ON database_config(config_name);
CREATE INDEX IF NOT EXISTS idx_database_config_type ON database_config(database_type);
CREATE INDEX IF NOT EXISTS idx_database_config_enabled ON database_config(enabled);


create TABLE IF NOT EXISTS setting (
    name TEXT PRIMARY KEY,
    description TEXT,
    value TEXT,
    enabled TEXT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);