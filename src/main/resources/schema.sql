CREATE TABLE IF NOT EXISTS stock_analyse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(64) NOT NULL,
    raw_response TEXT NOT NULL,
    requested_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_stock_analyse_requested_at
    ON stock_analyse (requested_at);
