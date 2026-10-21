-- 幣別對應表（題目要求：附建立 SQL 語法）
DROP TABLE IF EXISTS currency;

CREATE TABLE currency (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(10)  NOT NULL,
    chinese_name VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_currency_code UNIQUE (code)
);
