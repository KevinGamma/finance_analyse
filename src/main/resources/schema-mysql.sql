CREATE DATABASE IF NOT EXISTS finance_analyse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE finance_analyse;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_username (username)
);

CREATE TABLE IF NOT EXISTS stock_analyse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_code VARCHAR(64) NOT NULL,
    analysis_type VARCHAR(32) NOT NULL DEFAULT 'COMPREHENSIVE',
    raw_response TEXT NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    INDEX idx_stock_analyse_requested_at (requested_at)
);

CREATE TABLE IF NOT EXISTS news_analyse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(128) NOT NULL,
    raw_response TEXT NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    INDEX idx_news_analyse_requested_at (requested_at)
);
