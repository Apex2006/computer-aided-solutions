-- =====================================================================
-- Repository of Computer-Aided Solutions for Common Everyday Problems
-- schema.sql : creates the cas_db database and its 4 tables.
-- Matches ERD_CAS.drawio (board version): users, guides, reviews, reports.
-- Cloud version for TiDB Cloud Starter (also runs on MySQL 8).
--
-- BEFORE RUNNING: use Find and Replace to change YOUR_PREFIX into
-- the prefix of your TiDB user (the part before .root, for example
-- 2abc123XYZ) and change the cas_app password.
-- Run it in MySQL Workbench while logged in as YOUR_PREFIX.root.
-- WARNING: running it again deletes cas_db and every row inside it.
-- =====================================================================

DROP DATABASE IF EXISTS cas_db;
CREATE DATABASE cas_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE cas_db;

-- ---------------------------------------------------------------------
-- USERS: every account. Admin and Expert are values of role.
-- identity is a fixed list so the stratified expert rating has
-- exactly four groups. interests holds the niches picked at sign-up
-- (any of the 8 categories: Food and Cooking, Gardening and Planting,
-- Studies and Productivity, Housekeeping, Energy Saving, Home Security,
-- Health and Wellness, Pets). Expert fields stay NULL for normal users.
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL UNIQUE,
    email          VARCHAR(100) NOT NULL UNIQUE,
    password_hash  VARCHAR(60)  NOT NULL,            -- BCrypt hash, never the real password
    full_name      VARCHAR(100) NOT NULL,
    identity       ENUM('STUDENT', 'PROFESSIONAL', 'HOBBYIST', 'EDUCATOR') NOT NULL,
    interests      SET('FOOD_COOKING', 'GARDENING', 'STUDIES', 'HOUSEKEEPING',
                       'ENERGY_SAVING', 'HOME_SECURITY', 'HEALTH_WELLNESS', 'PETS') NOT NULL DEFAULT '',
    role           ENUM('USER', 'EXPERT', 'ADMIN') NOT NULL DEFAULT 'USER',
    expert_source  ENUM('VOTES', 'ADMIN') NULL,        -- VOTES = earned, ADMIN = added by admin
    credential     VARCHAR(150) NULL,                  -- required by the app when an admin adds an expert
    expert_since   DATETIME NULL,
    account_status ENUM('ACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- GUIDES: steps, parts and precautions are stored as text.
-- Drafts are guides with status DRAFT.
-- Expert approval lives here too: risk_factor, approval (Y/N),
-- approved_by and approved_at.
-- image holds the picture itself (the app allows up to 5 MB) so every
-- laptop sees it. category and status are indexed for fast browsing.
-- The search bar matches words in title and summary.
-- ---------------------------------------------------------------------
CREATE TABLE guides (
    guide_id       INT AUTO_INCREMENT PRIMARY KEY,
    approved_by    INT NULL,
    author_id      INT NOT NULL,
    title          VARCHAR(150) NOT NULL,
    category       ENUM('FOOD_COOKING', 'GARDENING', 'STUDIES', 'HOUSEKEEPING',
                        'ENERGY_SAVING', 'HOME_SECURITY', 'HEALTH_WELLNESS', 'PETS') NOT NULL,
    summary        TEXT NOT NULL,
    parts_list     TEXT NOT NULL,
    precautions    TEXT NOT NULL,
    steps          TEXT NOT NULL,
    image          MEDIUMBLOB NULL,
    schematic      MEDIUMBLOB NULL,
    step_images    LONGBLOB NULL,
    video_url      VARCHAR(500) NULL,
    estimated_cost DECIMAL(10, 2) NULL,
    difficulty     ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
    risk_factor    ENUM('LOW', 'MODERATE', 'HIGH') NOT NULL,
    status         ENUM('DRAFT', 'PENDING', 'PUBLISHED', 'HIDDEN') NOT NULL DEFAULT 'DRAFT',
    approval       ENUM('Y', 'N') NULL,
    approved_at    DATETIME NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_guides_category (category),
    INDEX idx_guides_status (status),
    CONSTRAINT fk_guides_author      FOREIGN KEY (author_id)   REFERENCES users (user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_guides_approved_by FOREIGN KEY (approved_by) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- REVIEWS: one row per person per guide holding their vote and their
-- comment. Either one can be empty: is_approved NULL means a comment
-- without a vote.
-- ---------------------------------------------------------------------
CREATE TABLE reviews (
    review_id   INT AUTO_INCREMENT PRIMARY KEY,
    guide_id    INT NOT NULL,
    user_id     INT NOT NULL,
    is_approved BOOLEAN NULL,
    comment     TEXT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_reviews_guide_user UNIQUE (guide_id, user_id),
    CONSTRAINT fk_reviews_guide FOREIGN KEY (guide_id) REFERENCES guides (guide_id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user  FOREIGN KEY (user_id)  REFERENCES users (user_id)   ON DELETE CASCADE
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- REPORTS: a user flags a guide (type REPORT) or asks for a change
-- (type EDIT_REQUEST). An admin resolves or dismisses both.
-- ---------------------------------------------------------------------
CREATE TABLE reports (
    report_id   INT AUTO_INCREMENT PRIMARY KEY,
    guide_id    INT NOT NULL,
    reporter_id INT NOT NULL,
    type        ENUM('REPORT', 'EDIT_REQUEST') NOT NULL DEFAULT 'REPORT',
    reason      TEXT NOT NULL,
    status      ENUM('OPEN', 'RESOLVED', 'DISMISSED') NOT NULL DEFAULT 'OPEN',
    resolved_by INT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reports_guide       FOREIGN KEY (guide_id)    REFERENCES guides (guide_id) ON DELETE CASCADE,
    CONSTRAINT fk_reports_reporter    FOREIGN KEY (reporter_id) REFERENCES users (user_id)   ON DELETE CASCADE,
    CONSTRAINT fk_reports_resolved_by FOREIGN KEY (resolved_by) REFERENCES users (user_id)   ON DELETE SET NULL
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- App login: the desktop app connects as cas_app, never as root.
-- cas_app can read and change rows but cannot drop or alter tables.
-- '%' lets the app connect from any network.
-- CHANGE THE PASSWORD BELOW before running, and put the same one
-- in the app's db.properties file.
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'YOUR_PREFIX.cas_app'@'%' IDENTIFIED BY 'CasApp@2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON cas_db.* TO 'YOUR_PREFIX.cas_app'@'%';

-- ---------------------------------------------------------------------
-- How the app saves an expert approval (first expert decision wins).
-- One UPDATE only touches the guide while it is still PENDING.
-- If it changes 0 rows, another expert already decided, so the app
-- shows "This guide was already reviewed by another expert."
--
--   Approve (Y):
--   UPDATE guides
--      SET approval = 'Y', status = 'PUBLISHED', risk_factor = ?,
--          approved_by = ?, approved_at = NOW()
--    WHERE guide_id = ? AND status = 'PENDING';
--
--   Reject (N): same, but approval = 'N' and status = 'DRAFT'.
-- ---------------------------------------------------------------------
