CREATE INDEX IF NOT EXISTS products_fts_gin_index on products USING gin (fulltext);
