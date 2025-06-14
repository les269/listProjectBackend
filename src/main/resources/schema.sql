create TABLE IF NOT EXISTS theme_header (
    header_id TEXT NOT NULL,
    name TEXT NOT NULL,
    version TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    update_time NUMERIC NOT NULL,
    theme_image TEXT NOT NULL,
    theme_label_list TEXT NOT NULL,
    theme_dataset_list TEXT NOT NULL,
    theme_custom_list TEXT NOT NULL,
    theme_tag_list TEXT NOT NULL,
    theme_other_setting TEXT NOT NULL,
    seq	INTEGER,
    PRIMARY KEY (header_id)
);

create TABLE IF NOT EXISTS theme_custom_value (
    header_id TEXT NOT NULL,
    by_key TEXT NOT NULL,
    correspond_data_value TEXT NOT NULL,
    custom_value TEXT,
    PRIMARY KEY ( header_id, by_key, correspond_data_value)
);

create TABLE IF NOT EXISTS theme_top_custom_value (
    header_id TEXT NOT NULL,
    by_key TEXT NOT NULL,
    custom_value TEXT,
    PRIMARY KEY ( header_id, by_key)
);

create TABLE IF NOT EXISTS theme_tag_value (
    header_id TEXT NOT NULL,
    tag TEXT NOT NULL,
    value_list TEXT NOT NULL,
    PRIMARY KEY ( header_id, tag)
);

create TABLE IF NOT EXISTS api_config (
    api_name TEXT NOT NULL,
    http_method TEXT NOT NULL,  -- 如 'GET', 'POST', 'PUT', 'DELETE'
    endpoint_url TEXT NOT NULL, -- API 的端點 URL
    request_body TEXT, -- 請求體，這裡假設是 JSON 格式
    http_params TEXT,
    http_headers TEXT,
    success_message TEXT,
    updated_time TEXT, -- 更新時間
    PRIMARY KEY (api_name)
);

create TABLE IF NOT EXISTS scrapy_config (
    name TEXT PRIMARY KEY,
    param_size INTEGER NOT NULL DEFAULT 1,
    data TEXT NOT NULL,
    test_json TEXT,
    test_url TEXT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create TABLE IF NOT EXISTS group_dataset_data (
    group_name TEXT NOT NULL,
    prime_value TEXT NOT NULL,
    json TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_name, prime_value)
);

CREATE INDEX IF NOT EXISTS  idx_group_name ON group_dataset_data(group_name);
CREATE INDEX IF NOT EXISTS  idx_prime_value ON group_dataset_data(prime_value);

create TABLE IF NOT EXISTS group_dataset (
    group_name TEXT NOT NULL,
    config TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_name)
);

create TABLE IF NOT EXISTS dataset (
    name TEXT,
    config TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);

create TABLE IF NOT EXISTS dataset_data (
    dataset_name TEXT,
    data TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (dataset_name)
);

create TABLE IF NOT EXISTS replace_value_map (
    name TEXT,
    map TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);

create TABLE IF NOT EXISTS scrapy_pagination (
    name TEXT,
    config TEXT NOT NULL,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (name)
);

create TABLE IF NOT EXISTS setting (
    name TEXT PRIMARY KEY,
    description TEXT,
    value TEXT,
    enabled TEXT,
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);