CREATE TABLE IF NOT EXISTS theme_header (
    header_id TEXT NOT NULL,
    name TEXT NOT NULL,
    version TEXT NOT NULL,
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    update_time NUMERIC NOT NULL,
    PRIMARY KEY (header_id)
);

CREATE TABLE IF NOT EXISTS theme_image (
    header_id TEXT NOT NULL,
    type TEXT NOT NULL,
    image_key TEXT,
    image_url TEXT,
    PRIMARY KEY (header_id),
    FOREIGN KEY (header_id) REFERENCES theme_header(header_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS theme_db (
    id INTEGER,
    header_id TEXT NOT NULL,
    type TEXT NOT NULL,
    label TEXT NOT NULL,
    source TEXT,
    groups TEXT,
    seq	TEXT,
    PRIMARY KEY (id, header_id),
    FOREIGN KEY (header_id) REFERENCES theme_header(header_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS theme_label (
    id INTEGER,
    header_id TEXT NOT NULL,
    type TEXT NOT NULL,
    seq TEXT,
    by_key TEXT,
    label TEXT,
    split_by TEXT,
    use_space TEXT,
    is_search_button NUMERIC,
    is_search_value	NUMERIC,
    is_copy NUMERIC,
    is_visible NUMERIC,
    is_sort NUMERIC,
    is_default_key NUMERIC,
    PRIMARY KEY (id, header_id),
    FOREIGN KEY (header_id) REFERENCES theme_header(header_id) ON DELETE CASCADE
);