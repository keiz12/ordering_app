-- ============================================================
--  Restaurant App Database Schema
--  Timezone: Africa/Kigali (UTC+2)
-- ============================================================

CREATE DATABASE IF NOT EXISTS ordering_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
    

USE ordering_app;

-- ============================================================
-- 1. USER TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    username    VARCHAR(100)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    PRIMARY KEY (id)
);

-- ============================================================
-- 2. AUTHORIZATION ROLES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS authorization_roles (
    id      BIGINT      NOT NULL AUTO_INCREMENT,
    role    ENUM(
                'BOSS',
                'MANAGER',
                'STAFF',
                'CUSTOMER'
            )           NOT NULL,
    user_id BIGINT      NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (user_id) REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 3. PRODUCT TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS product (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(150)    NOT NULL,
    description TEXT,
    price       DECIMAL(10, 2)  NOT NULL,
    PRIMARY KEY (id)
);

-- ============================================================
-- 4. PRODUCT IMAGE PATHS TABLE (One-to-Many)
-- ============================================================
CREATE TABLE IF NOT EXISTS product_image (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    product_id  BIGINT          NOT NULL,
    image_path  VARCHAR(500)    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (product_id) REFERENCES product(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 5. ORDER TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS `order` (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    uuid            VARCHAR(36)     NOT NULL UNIQUE,
    table_number    INT             NOT NULL,
    order_paid      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT (CONVERT_TZ(NOW(), 'UTC', 'Africa/Kigali')),
    PRIMARY KEY (id)
);

-- ============================================================
-- 6. ORDER PRODUCTS TABLE (Junction — Order ↔ Product)
-- ============================================================
CREATE TABLE IF NOT EXISTS order_products (
    order_id    BIGINT  NOT NULL,
    product_id  BIGINT  NOT NULL,
    PRIMARY KEY (order_id, product_id),
    FOREIGN KEY (order_id)   REFERENCES `order`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 7. STAFF PROCESSED ORDER TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS staff_processed_order (
    staff_id        BIGINT      NOT NULL,
    order_id        BIGINT      NOT NULL,
    processed_at    DATETIME    NOT NULL DEFAULT (CONVERT_TZ(NOW(), 'UTC', 'Africa/Kigali')),
    PRIMARY KEY (staff_id, order_id),
    FOREIGN KEY (staff_id)  REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (order_id)  REFERENCES `order`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 8. STAFF FAILED ORDER TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS staff_failed_order (
    staff_id    BIGINT  NOT NULL,
    order_id    BIGINT  NOT NULL,
    PRIMARY KEY (staff_id, order_id),
    FOREIGN KEY (staff_id)  REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (order_id)  REFERENCES `order`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 9. CUSTOMER FEEDBACK TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS customer_feedback (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    feedback        TEXT        NOT NULL,
    order_id        BIGINT      NOT NULL,
    table_number    INT         NOT NULL,
    submitted_at    DATETIME    NOT NULL DEFAULT (CONVERT_TZ(NOW(), 'UTC', 'Africa/Kigali')),
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES `order`(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ============================================================
-- 10. CUSTOMER ORDER KEY TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS customer_order_key (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    encrypted_key   VARCHAR(255)    NOT NULL,
    created_by      BIGINT          NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (created_by) REFERENCES user(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
